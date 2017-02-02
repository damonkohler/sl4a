package com.googlecode.android_scripting.facade.telephony;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/*
    }

    /**
     * Returns a particular call by its id.
     */
/*
    @Rpc(description = "Get call by particular Id")
    public Call telecomCallGetCallById(String callId) {
        return InCallServiceImpl.getCallById(callId);
    }
 */
    /**
     * Returns an identifier of the call. When a phone number is available, the number will be
     * returned. Otherwise, the standard object toString result of the Call object. e.g. A
     * conference call does not have a single number associated with it, thus the toString Id will
*/