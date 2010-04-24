package com.google.ase;

import java.util.List;

import com.google.ase.facade.FacadeConfiguration;
import com.google.ase.rpc.MethodDescriptor;

public class TextDocumentationGenerator {

  public static void main(String args[]) {
    List<MethodDescriptor> descriptors = FacadeConfiguration.collectRpcDescriptors();
    for (MethodDescriptor descriptor : descriptors) {
      System.out.println("{{{");
      System.out.println(descriptor.getHelp());
      System.out.println("}}}\n");
    }
  }

}
