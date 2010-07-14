package com.googlecode.android_scripting;

import java.util.List;

import org.apache.taglibs.string.util.StringW;


import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.rpc.MethodDescriptor;

public class TextDocumentationGenerator {

  public static void main(String args[]) {
    List<MethodDescriptor> descriptors = FacadeConfiguration.collectRpcDescriptors();
    for (MethodDescriptor descriptor : descriptors) {
      System.out.println("{{{");
      System.out.println(StringW.wordWrap(descriptor.getHelp()));
      System.out.println("}}}\n");
    }
  }

}
