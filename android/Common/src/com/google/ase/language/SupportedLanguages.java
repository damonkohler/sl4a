package com.google.ase.language;

import com.google.ase.AseLog;

import java.util.HashMap;
import java.util.Map;

public class SupportedLanguages {

  private static enum KnownLanguage {
    SHELL(".sh", ShellLanguage.class), 
    BEANSHELL(".bsh", BeanShellLanguage.class), 
    JAVASCRIPT(".js", JavaScriptLanguage.class), 
    LUA(".lua", LuaLanguage.class), 
    PERL(".pl", PerlLanguage.class), 
    PYTHON(".py", PythonLanguage.class),
    RUBY(".rb", RubyLanguage.class),
    TCL(".tcl", TclLanguage.class);

    private final String mmExtension;
    private final Class<? extends Language> mmClass;
    
    private KnownLanguage(String ext, Class<? extends Language> clazz) {
      mmExtension = ext;
      mmClass = clazz;
    }
    
    private String getExtension(){
      return mmExtension;
    } 
    
    private Class<? extends Language> getLanguageClass() {
      return mmClass;
    }
  }

  private static Map<String, Class<? extends Language>> sSupportedLanguages;

  static {
    sSupportedLanguages = new HashMap<String, Class<? extends Language>>();
    for (KnownLanguage lang : KnownLanguage.values()) {
      sSupportedLanguages.put(lang.getExtension(), lang.getLanguageClass());
    }
  }


  public static Language getLanguageByExtension(String extension) {
    extension = extension.toLowerCase();
    if (!extension.startsWith(".")) {
      extension = "." + extension;
    }
    Language lang = null;

    Class<? extends Language> clazz = sSupportedLanguages.get(extension);
    if (clazz != null) {
      try {
        lang = clazz.newInstance();
      } catch (IllegalAccessException e) {
        AseLog.e(e);
      } catch (InstantiationException e) {
        AseLog.e(e);
      }
    }
    return lang;
  }

  public static boolean checkLanguageSupported(String name) {
    String extension = name.toLowerCase();
    int index = extension.lastIndexOf('.');
    if (index < 0) {
      extension = "." + extension;
    } else if (index > 0) {
      extension = extension.substring(index);
    }
    return sSupportedLanguages.containsKey(extension);
  }
}
