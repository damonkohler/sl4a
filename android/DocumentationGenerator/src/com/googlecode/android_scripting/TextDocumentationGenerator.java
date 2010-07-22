package com.googlecode.android_scripting;

import com.googlecode.android_scripting.facade.BluetoothFacade;
import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.facade.SignalStrengthFacade;
import com.googlecode.android_scripting.facade.TextToSpeechFacade;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.MethodDescriptor;

import org.apache.taglibs.string.util.StringW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TextDocumentationGenerator {

  public static void main(String args[]) {
    List<MethodDescriptor> descriptors = FacadeConfiguration.collectRpcDescriptors();
    List<Class<? extends RpcReceiver>> classes =
        new ArrayList<Class<? extends RpcReceiver>>(FacadeConfiguration.getFacadeClasses());

    sortMethodDescriptors(descriptors);

    sortClasses(classes);

    HashMap<Class<? extends RpcReceiver>, Set<MethodDescriptor>> map =
        new HashMap<Class<? extends RpcReceiver>, Set<MethodDescriptor>>();

    for (MethodDescriptor descriptor : descriptors) {
      Class<? extends RpcReceiver> clazz = descriptor.getDeclaringClass();
      if (!map.containsKey(clazz)) {
        map.put(clazz, new HashSet<MethodDescriptor>());
      }
      map.get(clazz).add(descriptor);
    }

    for (Class<? extends RpcReceiver> clazz : classes) {

      int minSDK = getMinSdk(clazz);
      if (minSDK != 3) {
        System.out.println(String.format("*!%s* Requires API Level %d.", clazz.getSimpleName(),
            minSDK));
      } else {
        System.out.println(String.format("*!%s*", clazz.getSimpleName()));
      }
      List<MethodDescriptor> list = new ArrayList<MethodDescriptor>(map.get(clazz));
      sortMethodDescriptors(list);
      for (MethodDescriptor descriptor : list) {
        System.out.println(String.format("  * [#%1$s %1$s]", descriptor.getName()));
      }
    }

    System.out.println("\n");

    for (MethodDescriptor descriptor : descriptors) {
      System.out.println(String.format("===,,%s,,===", descriptor.getName()));
      System.out.println("{{{");
      System.out.println(StringW.wordWrap(descriptor.getHelp()));
      Class<? extends RpcReceiver> clazz = descriptor.getDeclaringClass();
      int minSDK = getMinSdk(clazz);

      if (minSDK != 3) {
        System.out.println(String.format("\nRequires API Level %d.", minSDK));
      }
      System.out.println("}}}\n");
    }
  }

  private static int getMinSdk(Class<? extends RpcReceiver> clazz) {
    if (clazz.isAssignableFrom(TextToSpeechFacade.class)) {
      return 4;
    } else if (clazz.isAssignableFrom(BluetoothFacade.class)) {
      return 5;
    } else if (clazz.isAssignableFrom(SignalStrengthFacade.class)) {
      return 7;
    }
    return 3;
  }

  private static void sortMethodDescriptors(List<MethodDescriptor> list) {
    Collections.sort(list, new Comparator<MethodDescriptor>() {
      @Override
      public int compare(MethodDescriptor md1, MethodDescriptor md2) {
        return md1.getName().compareTo(md2.getName());
      }
    });
  }

  private static void sortClasses(List<Class<? extends RpcReceiver>> list) {
    Collections.sort(list, new Comparator<Class<? extends RpcReceiver>>() {
      @Override
      public int compare(Class<? extends RpcReceiver> clazz1, Class<? extends RpcReceiver> clazz2) {
        return clazz1.getSimpleName().compareTo(clazz2.getSimpleName());
      }
    });
  }

}
