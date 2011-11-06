package com.googlecode.android_scripting;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Doclet to export SL4A API.
 * 
 * Combines javadoc and Rpc annotations into lightweight reference documentation.
 * 
 * Usage:
 * 
 * javadoc -dest <destination folder> -doclet com.googlecode.android_scripting.RpcDoclet -docletpath
 * <path to class> -sourcepath <sourcepathlist>
 * 
 * Or use javadoc.xml with the javadoc wizard in eclipse.
 * 
 * @author Robbie Matthews
 * 
 */
public class RpcDoclet {

  private RootDoc mRoot;
  private List<ClassDoc> classlist = new Vector<ClassDoc>();
  private String mDest;
  private PrintWriter mOutput;

  public RpcDoclet(RootDoc root) {
    mRoot = root;
  }

  public boolean run() {
    System.out.println("Started in " + System.getProperty("user.dir"));
    String temp;
    for (String[] list : mRoot.options()) {
      temp = "";
      if (list[0].equals("-dest")) {
        mDest = list[1];
      }
      for (String s : list) {
        temp += s + " : ";
      }
      addln(temp);
    }
    try {
      if (mDest == null) {
        System.out.println("Must define destination path (-dest <pathname<>)");
        return false;
      }
      File file = new File(mDest);
      if (!file.exists()) {
        file.mkdirs();
      }
      if (!file.isDirectory() || !file.canWrite()) {
        System.out.println("Can't write to destination path.");
        return false;
      }
      System.out.println("Classes " + mRoot.classes().length);
      for (ClassDoc clazz : mRoot.classes()) {
        if (hasRpc(clazz)) {
          classlist.add(clazz);
        }
      }
      if (classlist.isEmpty()) {
        System.out.println("No Rpc Classes found.");
        return false;
      }
      Collections.sort(classlist);
      createIndex(classlist);
      for (ClassDoc clazz : classlist) {
        dumpClassDetail(clazz);
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private String link(String href, String text) {
    if (text.startsWith("#")) {
      text = text.substring(1);
    }
    return "<a href=\"" + href + "\">" + text + "</a>";
  }

  private String anchor(String name) {
    return "<a name=\"" + name + "\"/>";
  }

  protected String tr(String contents) {
    return "<tr>" + contents + "</tr>";
  }

  protected String td(String contents) {
    return "<td>" + contents + "</td>";
  }

  private void createIndex(List<ClassDoc> classlist) throws IOException {
    File file = new File(mDest + "/index.html");
    mOutput = new PrintWriter(file);
    outputln("<html><head><title>SL4A API Help</title></head>");
    outputln("<body>");
    outputln("<h1>SL4A API Help</h1>");
    outputln("<table border=1>");
    for (ClassDoc clazz : classlist) {
      outputln("<tr><td>" + link(clazz.name() + ".html", clazz.name()) + "</td><td>"
          + trimComment(clazz.commentText()) + "</td></tr>");
    }
    outputln("</body></html>");
    mOutput.close();
  }

  private void dumpClassDetail(ClassDoc clazz) throws IOException {
    List<MethodDoc> methodlist = new Vector<MethodDoc>();
    addln(clazz.name());
    File file = new File(mDest + "/" + clazz.name() + ".html");
    mOutput = new PrintWriter(file);
    outputln("<html><head><title>SL4A API Help -" + clazz.name() + "</title></head>");
    outputln("<body>");
    outputln("<h1>SL4A API Help -" + clazz.name() + "</h1>");
    outputln(link("index.html", "index") + "<br>");
    outputln(expandTags(clazz));
    Map<String, AnnotationDesc> cmap = buildAnnotations(clazz.annotations());
    if (cmap.containsKey("RpcMinSdk")) {
      outputln("<br><i>Min SDK level=" + buildAnnotationDetails(cmap.get("RpcMinSdk")).get("value")
          + "</i>");
    }
    for (Tag t : clazz.tags()) {
      outputln("<br>" + t.name() + " " + t.text());
    }
    outputln("<table border=1>");
    for (MethodDoc method : clazz.methods()) {
      if (methodHasRpc(method)) {
        methodlist.add(method);
      }
    }
    Collections.sort(methodlist);
    for (MethodDoc method : methodlist) {
      output("<tr><td>" + anchor(method.name()) + "<b>" + method.name() + "</b></td>");
      output("<td>");
      Map<String, AnnotationDesc> amap = buildAnnotations(method);
      AnnotationDesc rpc = amap.get("Rpc");
      Map<String, String> mlist = buildAnnotationDetails(rpc);
      if (mlist.containsKey("description")) {
        outputln(htmlLines(mlist.get("description")));
      }
      listRpcParameters(method);
      if (mlist.containsKey("returns")) {
        outputln("<br><b>returns: (" + method.returnType().simpleTypeName() + ")</b> "
            + mlist.get("returns"));
      }
      if (method.commentText() != null && !method.commentText().isEmpty()) {
        outputln("<br>" + expandTags(method));
      }
      if (amap.containsKey("RpcMinSdk")) {
        outputln("<br><i>Min SDK level="
            + buildAnnotationDetails(amap.get("RpcMinSdk")).get("value") + "</i>");
      }
      if (amap.containsKey("RpcDeprecated")) {
        Map<String, String> dmap = buildAnnotationDetails(amap.get("RpcDeprecated"));

        outputln("<br><i>Deprecated in " + dmap.get("release") + ". Use " + dmap.get("value")
            + " instead.</i>");
      }
      outputln("</td></tr>");
    }
    outputln("</table>");
    outputln("<br>" + link("index.html", "index") + "<br>");
    outputln("</body></html>");
    mOutput.close();
  }

  private void listRpcParameters(MethodDoc m) throws IOException {
    AnnotationDesc annotation;
    Map<String, String> d;
    String s;
    for (Parameter parameter : m.parameters()) {
      Map<String, AnnotationDesc> paramList = buildAnnotations(parameter.annotations());
      if ((annotation = paramList.get("RpcParameter")) != null) {
        d = buildAnnotationDetails(annotation);
        String name = d.get("name");
        if (name == null) {
          name = parameter.name();
        }
        s = "<br><b>" + name + " (" + parameter.type().simpleTypeName() + ")</b> ";
        if (d.containsKey("description")) {
          s = s + htmlLines(d.get("description"));
        }
        if (paramList.containsKey("RpcOptional")) {
          s = s + " (optional)";
        }
        if (paramList.containsKey("RpcDefault")) {
          d = buildAnnotationDetails(paramList.get("RpcDefault"));
          s = s + " (default=" + d.get("value") + ")";
        }
        outputln(s);
      }
    }
  }

  private String expandTags(Doc doc) {
    StringBuilder result = new StringBuilder();
    for (Tag tag : doc.inlineTags()) {
      if (tag.name().equals("Text")) {
        result.append(tag.text());
      } else {
        StringTokenizer tokenizer = new StringTokenizer(tag.text());
        if (tag.kind().equals("@see")) {
          String link = tokenizer.nextToken();
          String name = (tokenizer.hasMoreTokens() ? tokenizer.nextToken() : link);
          result.append(link(link, name));
        } else {
          result.append("[" + tag.name() + ":" + tag.text() + "]");
        }
      }
    }
    return result.toString();
  }

  private Map<String, AnnotationDesc> buildAnnotations(AnnotationDesc[] annotations) {
    Map<String, AnnotationDesc> result = new LinkedHashMap<String, AnnotationDesc>();
    for (AnnotationDesc annotation : annotations) {
      result.put(annotation.annotationType().name(), annotation);
    }
    return result;
  }

  private Map<String, AnnotationDesc> buildAnnotations(ProgramElementDoc doc) {
    return buildAnnotations(doc.annotations());
  }

  private String trimQuotes(String value) {
    if (!value.startsWith("\"")) {
      return value;
    }
    return value.substring(1, value.lastIndexOf("\""));
  }

  private Map<String, String> buildAnnotationDetails(AnnotationDesc annotation) {
    Map<String, String> result = new HashMap<String, String>();
    for (ElementValuePair e : annotation.elementValues()) {
      result.put(e.element().name(), trimQuotes(e.value().toString()));
    }
    return result;
  }

  private String trimComment(String commentText) {
    int i = commentText.indexOf(".");
    if (i > 0) {
      return commentText.substring(0, i);
    }
    return commentText;
  }

  private String htmlLines(String value) {
    addln(value);
    return value.replace("\\n", "<br>").replace("\n", "<br>");
  }

  private void addln(Object message) {
    System.out.println(message);
  }

  private void output(String message) throws IOException {
    mOutput.print(message);
  }

  private void outputln(String message) throws IOException {
    mOutput.println(message);
  }

  private boolean hasRpc(ClassDoc clazz) {
    for (MethodDoc method : clazz.methods()) {
      if (methodHasRpc(method)) {
        return true;
      }
    }
    return false;
  }

  private boolean methodHasRpc(MethodDoc method) {
    for (AnnotationDesc a : method.annotations()) {
      if (a.annotationType().name().equals("Rpc")) {
        return true;
      }
    }
    return false;
  }

  public Map<String, Object> sortmap(Map<String, Object> map) {
    Set<String> list = new TreeSet<String>(map.keySet());
    Map<String, Object> result = new LinkedHashMap<String, Object>();
    for (String key : list) {
      result.put(key, map.get(key));
    }
    return result;
  }

  public static boolean start(RootDoc root) {
    RpcDoclet mydoclet = new RpcDoclet(root);
    return mydoclet.run();
  }

  public static int optionLength(String option) {
    if (option.equals("-dest")) {
      return 2;
    }
    return 0;
  }
}
