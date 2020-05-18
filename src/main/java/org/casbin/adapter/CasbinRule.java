package org.casbin.adapter;

import java.io.Serializable;

public class CasbinRule implements Serializable{
    String ptype;
    String v0;
    String v1;
    String v2;
    String v3;
    String v4;
    String v5;

    public CasbinRule() {
    }

    public CasbinRule(String ptype, String v0, String v1, String v2, String v3, String v4, String v5) {
        this.ptype = ptype;
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.v4 = v4;
        this.v5 = v5;
    }

    public String getPtype() {
        return ptype;
    }

    public void setPtype(String ptype) {
        this.ptype = ptype;
    }

    public String getV0() {
        return v0;
    }

    public void setV0(String v0) {
        this.v0 = v0;
    }

    public String getV1() {
        return v1;
    }

    public void setV1(String v1) {
        this.v1 = v1;
    }

    public String getV2() {
        return v2;
    }

    public void setV2(String v2) {
        this.v2 = v2;
    }

    public String getV3() {
        return v3;
    }

    public void setV3(String v3) {
        this.v3 = v3;
    }

    public String getV4() {
        return v4;
    }

    public void setV4(String v4) {
        this.v4 = v4;
    }

    public String getV5() {
        return v5;
    }

    public void setV5(String v5) {
        this.v5 = v5;
    }

    @Override
    public String toString() {
        return "CasbinRule{" +
                "ptype='" + ptype + '\'' +
                ", v0='" + v0 + '\'' +
                ", v1='" + v1 + '\'' +
                ", v2='" + v2 + '\'' +
                ", v3='" + v3 + '\'' +
                ", v4='" + v4 + '\'' +
                ", v5='" + v5 + '\'' +
                '}';
    }
}
