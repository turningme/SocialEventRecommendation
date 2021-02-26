package org.turningme.theoretics.api;

import java.io.Serializable;

public class RecommendResult implements Serializable {
    static final long serialVersionUID = 1L;

    StringBuffer stringBuffer;

    public RecommendResult() {
        stringBuffer = new StringBuffer("RecommendResult").append("\n");
    }

    public void append(String msg){
        stringBuffer.append("-msg ").append(msg).append("\n");
    }

    @Override
    public String toString() {
        return "RecommendResult{" +
                "stringBuffer=" + stringBuffer.toString() +
                '}';
    }
}
