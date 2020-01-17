package com.jetbrains;

import java.util.*;

public class Gene {

    /*Infos die ich im Gen haben will:
    * Array GenInfo: geneId + geneSymbol + chromosom + strand + nprots + ntrans
    * HashMap Exons: key -> protId, value -> ArrayList mit Sections
    * TODO: können die gleichen Sections mehrmals auftreten?
    * HashMap Introns: key -> protId, value -> ArrayList mit Sections
    *Set transcripts: Set die transcript_Ids beinhaltet um ntrans zu errechnen

    * Methode die Introns berechnet
    *  */

    //Array GenInfo
    protected String[] GenInfo = new String[6];

    //HashMap Exons
    protected HashMap<String, ArrayList<Section>> Exons = new HashMap<>();

    //TODO: setter verwenden
    public void setIntrons(HashMap<String, ArrayList<Section>> introns) {
        Introns = introns;
    }

    //HashMap Introns
    protected HashMap<String, ArrayList<Section>> Introns = new HashMap<>();

    //Set transcripts
    protected HashSet<String> transcriptIDs = new HashSet<>();

    public Gene(String[] genInfo, HashMap<String, ArrayList<Section>> exons, HashMap<String, ArrayList<Section>> introns, HashSet<String> transcriptIDs) {
        GenInfo = genInfo;
        Exons = exons;
        Introns = introns;
        this.transcriptIDs = transcriptIDs;
    }

    public String[] getGenInfo() {
        return GenInfo;
    }

    public HashMap<String, ArrayList<Section>> getExons() {
        return Exons;
    }

    public HashMap<String, ArrayList<Section>> getIntrons() {
        return Introns;
    }

    public HashSet<String> getTranscriptIDs() {
        return transcriptIDs;
    }

    public String genInfoAufbereitung(String[] GenInfo){
        String Info = "";
        Info += GenInfo[0] +"\t" + GenInfo[1] + "\t" + GenInfo[2] + "\t" + GenInfo[3] + "\t" + GenInfo[4] + "\t" + GenInfo[5] + "\t";
        return Info;
    }

    @Override
    public String toString() {
        return "Gene{" +
                "GenInfo=" + Arrays.toString(GenInfo) +
                ", Exons=" + Exons +
                ", Introns=" + Introns +
                ", transcriptIDs=" + transcriptIDs +
                '}';
    }

    //man gibt seine exons ein und bekommt seine introns raus
    public HashMap<String, ArrayList<Section>> calculateIntrons(HashMap<String, ArrayList<Section>> exis){
        HashMap<String, ArrayList<Section>> inis = new HashMap<>();
        Iterator<HashMap.Entry<String, ArrayList<Section>>> itr = exis.entrySet().iterator();

        while(itr.hasNext()){
            //proteinId holen als Key
            String proteinID = itr.next().getKey();
            //alle Sections durchgehen -> damit dann die Introns berechnen
            ArrayList<Section> proteinSectiExons = exis.get(proteinID);

            //man muss die ArrayList noch sortieren
            //TODO: ist es nicht schlauer die Liste schon vorher zu sortieren?
            Collections.sort(proteinSectiExons);
            ArrayList<Section> proteinSectiIntrons = new ArrayList<>();

            //ich setze den Anfang auf den ersten Anfang

            int endingBefore = 0;
            for(int i = 0; i<proteinSectiExons.size(); i++){
                //wenn man ganz am Anfang ist -> das Ending before festsetzen
                if(i == 0){
                    endingBefore = proteinSectiExons.get(i).getEnding();

                }
                else{
                    //Achtung Intronstart = Exonende +1 und Intronende = Exonstart
                    Section Exon = proteinSectiExons.get(i);
                    Section Intron = new Section(endingBefore+1, Exon.getBeginning());
                    proteinSectiIntrons.add(Intron);
                    endingBefore = Exon.getEnding();
                    //TODO: fehlen dann wirklich keine Introns? was ist mit Ende?
                }
            }
            //TODO: hätte ich das nochmal sortieren müssen?
            Collections.sort(proteinSectiIntrons);
            inis.put(proteinID, proteinSectiIntrons);

        }
        return inis;
    }

    
    public Object[] skippedExonBerechnen(Section SV, String SVprotId, HashMap<String, ArrayList<Section>> inis) {
        //SVSectionSet erstellen
        HashSet<Section> SVIntron = new HashSet<>();
        SVIntron.add(SV);

        //Liste mit allen möglichen SVIntronsprotIDs
        ArrayList<String> SVProts = new ArrayList<>();
        SVProts.add(SVprotId);


        //SV Anfang und Ende holen
        int start = SV.getBeginning();
        int end = SV.getEnding();



        //HashMap mit WTprotID und HashSet<Section> mit WTIntrons bei skippedExon Event
        HashMap<String, HashSet<Section>> WTSE = new HashMap<>();

        //min und maxs
        int minNum = 0;
        int maxNum = 0;
        int minLen = 0;
        int maxLen = 0;

        //HashMap durchgehen
        Iterator<HashMap.Entry<String, ArrayList<Section>>> itr = inis.entrySet().iterator();
            //jetzt schaue ich ein WTGen an
            while (itr.hasNext()) {
            //Name und Sections holen
            String WTGenProteinID = itr.next().getKey();
            if(WTGenProteinID != SVprotId){
                ArrayList<Section> IntronsWT = inis.get(WTGenProteinID);

                //ein Set mit allen Intron Section des WTs
                HashSet<Section> WTaktuell = new HashSet<>();
                int current = 0;
                //jetzt Intron für Intron von WTGen durchgehen und mit SVIntron vergleichen
                for (int j = 0; j < IntronsWT.size(); j++) {
                    Section WTIntron = IntronsWT.get(j);
                    int WTstart = WTIntron.getBeginning();
                    int WTend = WTIntron.getEnding();

                //Intron ist genau dasselbe -> kein Exonskipping
                //ODER WTIntron liegt genau zwischen SVIntron


                    if (start <= WTstart && WTend <= end) {
                        WTaktuell.add(WTIntron);
                        if(WTstart == start){
                            current = current+1;

                        }
                        if(WTend == end){
                            current = current +1;

                        }
                    }

                }

                int wirklichLeer = 0;
                if(WTaktuell.size() ==0){
                    wirklichLeer = wirklichLeer +1;
                }
                WTaktuell.removeAll(SVIntron);




            //Sets zusammenlegen

              /* int really = 0;
                Iterator all = WTaktuell.iterator();
                while(all.hasNext()) {
                    Section aktuell = (Section) all.next();
                    if (aktuell.getBeginning() == start) {
                        really = really + 1;
                    }
                    if (aktuell.getEnding() == end) {
                        really = really + 1;
                    }
                }
                if(really !=2){
                    WTaktuell.clear();
                }*/



            //wenn skippedExonEvent stattgefunden hat: WTaktuell ist nicht leer
                if(wirklichLeer != 1){
            if (!WTaktuell.isEmpty()) {
                //füge der HashMap das skippedExonEvent hinzu
                if(current == 2){
                WTSE.put(WTGenProteinID, WTaktuell);
                current = 0;}

            }
            //falls ExonSkipping stattgefunden hat in anderer Genvariante, wäre dieses dann ein weiteres SVIntron
            else {
                //TODO: achtung hier werden auch die hinzugefügt die einfach vorher schon leer waren ohne dass sie SVs sind
                SVProts.add(WTGenProteinID);
            }}
           }

        }
        Object[] ergebnis = new Object[2];
        ergebnis[0] =WTSE;
        ergebnis[1] = SVProts;
        
        return ergebnis;

    }

    //wenn WTSE nicht leer ist -> Skipped Exons erstellen, wenn nicht dann WTSE löschen
    public SkippedExons skippedExonErstellen(Object[] ergebnis, HashMap<String, ArrayList<Section>> exi, Section SV){
            //SVSection + WTHashMapSections + SVSet + WTHashMap Key WT
            HashMap<String,HashSet<Section>> WTSE = (HashMap<String, HashSet<Section>>) ergebnis[0];
            ArrayList<String> SVProt = (ArrayList<String>) ergebnis[1];

            ArrayList<String> WTProt = new ArrayList<>();
            HashSet<Section> WT = new HashSet<>();
            int minNum = Integer.MAX_VALUE;
            int maxNum = 0;
            int minLen = Integer.MAX_VALUE;
            int maxLen = 0;

            Iterator<HashMap.Entry<String, HashSet<Section>>> itrSmall= WTSE.entrySet().iterator();
            while(itrSmall.hasNext()) {

                //IDs zu WTProt hinzufügen
                String WTIntronProtIdAktuell = itrSmall.next().getKey();
                WTProt.add(WTIntronProtIdAktuell);

                //Introns zu WT hinzufügen
                HashSet<Section> WTInSet = WTSE.get(WTIntronProtIdAktuell);
                WTInSet.stream().forEach(section -> WT.add(section));
                /*Iterator itrS= WTInSet.iterator();
                while (itrS.hasNext()){
                    Section WTIntron = (Section)itrS.next();
                    WT.add(WTIntron);
                }*/

                //die Längen und die Anzahl der Exons suchen
                ArrayList<Section> exons = exi.get(WTIntronProtIdAktuell);
                int start = SV.getBeginning();
                int end = SV.getEnding();
                int counter = 0;
                int length = 0;
                for(int i = 0; i<exons.size(); i++){
                    int startExon = exons.get(i).getBeginning();
                    int endExon = exons.get(i).getEnding();

                    if(start <=startExon && endExon <=end){
                        counter= counter +1;
                        length = (endExon-startExon) + length +1;
                    }
                    //exon fängt schon vor intron an, rechne länge mit start ab SVIntron
                    if(startExon<start && start< endExon && endExon<=end){
                        counter= counter +1;
                        length = (endExon-start) + length +1;
                    }
                }

                //eventuell die min und max neu setzen
                if(counter > maxNum){
                    maxNum =counter;
                }
                if(counter< minNum){
                    minNum=counter;
                }
                if(length>maxLen){
                    maxLen=length;
                }
                if(length<minLen){
                    minLen = length;
                }
                counter = 0;
                length = 0;

            }
        SkippedExons skipi = new SkippedExons(SV, WT, SVProt, WTProt, maxNum, minNum,maxLen,minLen);
        return skipi;

    }

}
