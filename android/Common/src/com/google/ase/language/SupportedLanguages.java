package com.google.ase.language;

import com.google.ase.AseLog;
import com.google.ase.exception.AseException;

import java.util.HashMap;
import java.util.Map;

public class SupportedLanguages {

  private static enum KnownLanguage {
//    SHELL(".sh", ShellLanguage.class), // We don't really support Shell language
    BEANSHELL(".bsh", BeanShellLanguage.class), 
    JAVASCRIPT(".js", JavaScriptLanguage.class), 
    LUA(".lua", LuaLanguage.class), 
    PERL(".pl", PerlLanguage.class), 
    PYTHON(".py", PythonLanguage.class), 
    RUBY(".rb", RubyLanguage.class),
    TCL(".tcl", TclLanguage.class),
    PHP(".php", PhpLanguage.class);

    private final String mmExtension;
    private final Class<? extends Language> mmClass;

    private KnownLanguage(String ext, Class<? extends Language> clazz) {
      mmExtension = ext;
      mmClass = clazz;
    }

    private String getExtension() {
      return mmExtension;
    }

    private Class<? extends Language> getLanguageClass() {
      return mmClass;
    }
  }

  private static Map<String, Class<? extends Language>> sSupportedLanguages;

  static {
    sSupportedLanguages = new HashMap<String, Class<? extends Language>>();
    for (KnownLanguage language : KnownLanguage.values()) {
      sSupportedLanguages.put(language.getExtension(), language.getLanguageClass());
    }
  }

  public static Language getLanguageByExtension(String extension) throws AseException {
    extension = extension.toLowerCase();
    if (!extension.startsWith(".")) {
      throw new AseException("Extension does not start with a dot: " + extension);
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
