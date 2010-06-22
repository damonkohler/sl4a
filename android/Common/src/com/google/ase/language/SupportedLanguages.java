package com.google.ase.language;

import java.util.HashMap;
import java.util.Map;

public class SupportedLanguages {

  private enum KnownLanguage {
    SHELL, BEANSHELL, JAVASCRIPT, LUA, PERL, PYTHON, RUBY, TCL
  }

  private static Map<String, KnownLanguage> mSupportedLanguages;
  static {
    mSupportedLanguages = new HashMap<String, KnownLanguage>();

    mSupportedLanguages.put(".sh", KnownLanguage.SHELL);
    mSupportedLanguages.put(".bsh", KnownLanguage.BEANSHELL);
    mSupportedLanguages.put(".js", KnownLanguage.JAVASCRIPT);
    mSupportedLanguages.put(".lua", KnownLanguage.LUA);
    mSupportedLanguages.put(".pl", KnownLanguage.PERL);
    mSupportedLanguages.put(".py", KnownLanguage.PYTHON);
    mSupportedLanguages.put(".rb", KnownLanguage.RUBY);
    mSupportedLanguages.put(".tcl", KnownLanguage.TCL);
  }

  public static Language getLanguageByExtention(String extention) {
    extention = extention.toLowerCase();
    if (!extention.startsWith(".")) {
      extention = "." + extention;
    }
    KnownLanguage language = mSupportedLanguages.get(extention);
    switch (language) {
    case SHELL:
      return new ShellLanguage();
    case BEANSHELL:
      return new BeanShellLanguage();
    case JAVASCRIPT:
      return new JavaScriptLanguage();
    case LUA:
      return new LuaLanguage();
    case PERL:
      return new PerlLanguage();
    case PYTHON:
      return new PythonLanguage();
    case RUBY:
      return new RubyLanguage();
    case TCL:
      return new TclLanguage();
    default:
      return new Language() {
      };
    }
  }
}
