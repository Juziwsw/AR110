package com.hiar.ar110.data;

public class LocationReportRes {
    public int retCode;
    public NodeInfo data;

    public class NodeInfo {
        public int nodeId;
        public String cjdbh2;

        @Override
        public String toString() {
            return "NodeInfo{" +
                "nodeId=" + nodeId +
                ", cjdbh2='" + cjdbh2 + '\'' +
                '}';
        }
    }
}
