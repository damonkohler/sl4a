package com.googlecode.android_scripting;

import com.googlecode.android_scripting.facade.BluetoothFacade;
import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.facade.SignalStrengthFacade;
import com.googlecode.android_scripting.facade.TextToSpeechFacade;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.MethodDescriptor;

import org.apache.taglibs.string.util.StringW;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TextDocumentationGenerator {

  public static void main(String args[]) {
    List<MethodDescriptor> descriptors = FacadeConfiguration.collectRpcDescriptors();

    Collections.sort(descriptors, new Comparator<MethodDescriptor>() {
      @Override
      public int compare(MethodDescriptor md1, MethodDescriptor md2) {
        return md1.getName().compareTo(md2.getName());
      }
    });

    System.out.println("----");
    int i = 0;
    for (MethodDescriptor descriptor : descriptors) {
      if (i % 3 == 0) {
        System.out.print("\n||");
      }
      System.out.print(String.format(" [#%1$s %1$s] ||", descriptor.getName()));
      i++;
    }

    System.out.println("\n\n----");

    for (MethodDescriptor descriptor : descriptors) {
      System.out.println(String.format("===,,%s,,===", descriptor.getName()));
      System.out.println("{{{");
      System.out.println(StringW.wordWrap(descriptor.getHelp()));
      Class<? extends RpcReceiver> clazz = descriptor.getDeclaringClass();
      int minSDK = 3;
      if (clazz.isAssignableFrom(TextToSpeechFacade.class)) {
        minSDK = 4;
      } else if (clazz.isAssignableFrom(BluetoothFacade.class)) {
        minSDK = 5;
      } else if (clazz.isAssignableFrom(SignalStrengthFacade.class)) {
        minSDK = 7;
      }

      if (minSDK != 3) {
        System.out.println(String.format("\nRequires API Level %d.", minSDK));
      }
      System.out.println("}}}\n");
    }
  }
}
