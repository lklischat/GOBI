package com.jetbrains;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SkippedExons {
    //Section SV-Intron:  mit Start und Ende
    //ArrayList<Section> WT Introns: Wt Introns innerhalb SV Intron mit Start und Ende
    //ArrayList<String> protID-SV-Introns
    //ArrayList<String> protID-WT-Introns
    //String maxNumSE: max Anzahl an geskippten Exons
    //String minNumSE: min Anzahl an geskippten Exons
    //String maxLenSE: max Länge(Basenanzahl) bei geskippten Exons
    //String minLenSE: min Länge(Basenanzahl) bei geskippten Exons
    //

    private Section SV;
    private HashSet<Section> WT;
    private ArrayList<String> SVProts;
    private ArrayList<String> WTProts;

    private int maxNumSE;
    private int minNumSE;
    private int maxLenSE;
    private int minLenSE;


    public SkippedExons(Section SV, HashSet<Section> WT, ArrayList<String> SVProts, ArrayList<String> WTProts, int maxNumSE, int minNumSE, int maxLenSE, int minLenSE) {
        this.SV = SV;
        this.WT = WT;
        this.SVProts = SVProts;
        this.WTProts = WTProts;
        this.maxNumSE = maxNumSE;
        this.minNumSE = minNumSE;
        this.maxLenSE = maxLenSE;
        this.minLenSE = minLenSE;
    }

    @Override
    public int hashCode() {
        String s = SV.toString() + WT.toString() + SVProts.toString() + WTProts.toString();
        return s.hashCode();
    }

    public String wtAufbereitung(HashSet<Section> WT){
        String WTs="";
        for(Section sect : WT){
            WTs += sect.toString()+"|";
        }
        int WTsLength = WTs.length();
        WTsLength = WTsLength -1;
        WTs = WTs.substring(0, WTsLength);
        return WTs;
    }

    public String idAufbereitung(ArrayList<String> IDs){
        String ProtId = "";
        for(String id : IDs){
            ProtId += id + "|";
        }
        int ProtIDLength = ProtId.length() -1;
        ProtId = ProtId.substring(0, ProtIDLength);
        return ProtId;
    }


    @Override
    public String toString() {
        return SV.toString() + "\t" +
                wtAufbereitung(WT) + "\t" +
                idAufbereitung(SVProts) + "\t" +
                idAufbereitung(WTProts) + "\t" +
                maxNumSE + "\t" +
                minNumSE + "\t" +
                maxLenSE + "\t" +
                minLenSE
                ;
    }
}
