package com.google.ase;

public interface RpcFacade {
  /**
   * Invoked when the facade is initialized.
   */
  public void initialize();
  
  /**
   * Invoked when the facade is shut down.
   */
  public void shutdown();
}
