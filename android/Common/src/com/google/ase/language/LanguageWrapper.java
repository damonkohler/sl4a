package com.google.ase.language;

import java.util.Map;

public class LanguageWrapper extends Language implements LanguageStrings {

  private final String mImportStatement;
  private final String mRpcReceiverDeclaration;
  private final String mRpcReceiver;
  private final String mQuote;
  private final String mNull;
  private final String mTrue;
  private final String mFalse;
  private final String mApplyReceiverText;
  private final String mApplyOperator;
  private final String mLeftParameters;
  private final String mRightParameters;
  private final String mParameterSeparator;

  private LanguageWrapper(Map<String, String> data) {
    mImportStatement = data.get(IMPORT);
    mNull = data.get(NULL);
    mTrue = data.get(TRUE);
    mFalse = data.get(FALSE);
    mQuote = data.get(QUOTE);
    mApplyOperator = data.get(APPLY_OPERATOR);
    mApplyReceiverText = data.get(APPLY_RECEIVER_TEXT);
    mRpcReceiver = data.get(RPC_RECEIVER);
    mRpcReceiverDeclaration = data.get(RPC_RECEIVER_DECLARATION);
    mLeftParameters = data.get(LEFT_PARAMS);
    mRightParameters = data.get(RIGHT_PARAMS);
    mParameterSeparator = data.get(PARAM_SEPARATOR);
  }

  public static Language extractFromMap(Map<String, String> data) {
    if (data == null) {
      return new Language() {
      };
    }
    return new LanguageWrapper(data);
  }

  @Override
  protected String getImportStatement() {
    if (mImportStatement == null) {
      return super.getImportStatement();
    }
    return mImportStatement;
  }

  @Override
  protected String getRpcReceiverDeclaration(String rpcReceiver) {
    if (mRpcReceiverDeclaration == null) {
      return super.getRpcReceiverDeclaration(rpcReceiver);
    }
    return String.format(mRpcReceiverDeclaration, rpcReceiver);
  }

  @Override
  protected String getDefaultRpcReceiver() {
    if (mRpcReceiver == null) {
      return super.getDefaultRpcReceiver();
    }
    return mRpcReceiver;
  }

  @Override
  protected String getQuote() {
    if (mQuote == null) {
      return super.getQuote();
    }
    return mQuote;
  }

  {
  }

  @Override
  protected String getNull() {
    if (mNull == null) {
      return super.getNull();
    }
    return mNull;
  }

  @Override
  protected String getTrue() {
    if (mTrue == null) {
      return super.getTrue();
    }
    return mTrue;
  }

  @Override
  protected String getFalse() {
    if (mFalse == null) {
      return super.getFalse();
    }
    return mFalse;
  }

  @Override
  protected String getApplyReceiverText(String receiver) {
    if (mApplyReceiverText == null) {
      return super.getApplyReceiverText(receiver);
    }
    return String.format(mApplyReceiverText, receiver);
  }

  @Override
  protected String getApplyOperatorText() {
    if (mApplyOperator == null) {
      return super.getApplyOperatorText();
    }
    return mApplyOperator;
  }

  @Override
  protected String getLeftParametersText() {
    if (mLeftParameters == null) {
      return super.getLeftParametersText();
    }
    return mLeftParameters;
  }

  @Override
  protected String getRightParametersText() {
    if (mRightParameters == null) {
      return super.getRightParametersText();
    }
    return mRightParameters;
  }

  @Override
  protected String getParameterSeparator() {
    if (mParameterSeparator == null) {
      return super.getParameterSeparator();
    }
    return mParameterSeparator;
  }

}
