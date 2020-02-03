package com.jetbrains;

import java.io.*;
import java.util.*;
import org.apache.commons.cli.*;

public class Main {



        public static Options oparser() {
            // ich erstelle das Opjekt Options
            Options options = new Options();

            options.addOption(Option.builder().longOpt("gtf")
                    .hasArg() //hinter gtf steht noch ein Argument
                    .required() //es muss gtf geben
                    .numberOfArgs(1) // es kommt ein Argument nach gtf
                    .build() //erstellen der Option
            );

            options.addOption(Option.builder().longOpt("o")
                    .hasArg()
                    .required()
                    .numberOfArgs(1)
                    .build());

            //ich gebe das Objekt options zurück
            return options;
        }

        public static void main (String[]args) throws ParseException, IOException /*oder throws IOException für beide?*/
        {

            //TODO: Exceptions einbauen
            //TODO: evtl Stringbuilder benutzen

            String inputpath;
            String outputpath;
            //ArrayList zum speichern der notwendingen Lines der Datei
            ArrayList<String[]> allNecessaryLines = new ArrayList<String[]>();

            // ich hole mir meine Optionen durch den vorher erstellten Optionparser
            Options options = oparser();
            CommandLineParser parser = new DefaultParser();
            //der parser braucht die options, die ich vorher erstellt hatte und die args aus String[] args aus der main Methode
            CommandLine cmdl = parser.parse(options, args);
            //nun hole ich mir die einzelnen Argumente aus Objekts und speichere sie in meine vorher erstellten Strings
            //diese sind aber nur die Wege zu meinen Files -> jetzt brauche ich noch einen reader der meine Datei liest
            inputpath = cmdl.getOptionValue("gtf");
            outputpath = cmdl.getOptionValue("o");

            BufferedReader in = new BufferedReader(new FileReader(inputpath));
            // eventuell das ganze mit:  BufferedReader in = new BufferedReader(new FileReader(new File(inputpath)));

            //HasMap erstellen mit allen Infos zu den Genen
            //HashMap <String, Gene> allGenes = new HashMap<>();

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputpath));
            //Überschrift
            writer.write("id\tsymbol\tchr\tstrand\tnprots\tntrans\tSV\tWT\tWT_prots\tSV_prots\tmin_skipped_exon\tmax_skipped_exon\tmin_skipped_bases\tmax_skipped_bases\n");


            // ich habe jetzt einen BufferedReader der mir characters streamt. Jetzt muss ich einfach Zeile fuer Zeile durchgehen um mein Infos zu bekommen
            // ACHTUNG! man muss die line schon vorher erstellen und dann wi unten, sonst liest bufferedreader nur jede zweite zeile
            String line = null;

            //aktulles Gen erstellen, denn alles zu einem Gen kommt immer untereinander
            Gene aktuellesGen = null;

            while ((line = in.readLine()) != null) {
                //String line = in.readLine();
                // den String tab orientiert splitten
                if (line.startsWith("#") == false) {
                    String[] splitline = line.split("\t");
                    //man will nur die Zeilen speichern mit CDS
                    //Achtung: bei == ist nur Vergleich des Speicherorts und nicht die lexikalische Übereinstimmung -> man braucht equals
                    if (splitline[2].equals("CDS")) {
                        allNecessaryLines.add(splitline);
                        String Attribute = splitline[8];
                        String[] Attri = Attribute.split("; ");
                        String protein_id = null;
                        String gene_id = null;
                        String gene_symbol = null;
                        String trancsript_id = null;
                        Section section = new Section(Integer.parseInt(splitline[3]), Integer.parseInt(splitline[4]));
                        //TODO: alle notwendigen Infos von den Attributen holen
                        //Infos die ich von Attribute brauche: gene_ID, gene_name, transcript_id, protein_id
                        for (int j = 0; j < Attri.length; j++) {
                            String item = Attri[j];
                            if (item.startsWith("gene_id ")) {

                                String gene_id_unfinished = item;
                                gene_id =gene_id_unfinished.split("\"")[1];
                            }
                            if (item.startsWith("protein_id ")) {
                                String protein_id_unfinished = item;
                                protein_id =protein_id_unfinished.split("\"")[1];

                            }

                            if (item.startsWith("transcript_id ")) {

                                String trancsript_id_unfinished = item;
                                trancsript_id =trancsript_id_unfinished.split("\"")[1];

                            }
                            if (item.startsWith("gene_name ")) {
                                String gene_symbol_unfinished = item;
                                gene_symbol =gene_symbol_unfinished.split("\"")[1];

                            }
                        }
                        //nun alle Infos in die HashMap
                        //Fall 1: Gene ist bereits vorhanden -> nur noch erweitern
                        //bei der allerersten Zeile

                        if(aktuellesGen == null){
                            String Chromosom = splitline[0];
                            String Strand = splitline[6];

                            //HashMap Exons erstellen
                            HashMap<String, ArrayList<Section>> exons = new HashMap<>();
                            ArrayList<Section> firstSectionEver = new ArrayList<>();
                            firstSectionEver.add(section);
                            exons.put(protein_id, firstSectionEver);

                            //HashMap Introns erstellen
                            HashMap<String, ArrayList<Section>> introns = new HashMap<>();

                            //Set mit transcript_ID erstellen
                            HashSet<String> transcripts = new HashSet<>();
                            transcripts.add(trancsript_id);

                            //Gen erstellen und Gen der HashMap allGenes hinzufügen
                            Gene newGene = new Gene(gene_id, gene_symbol, Chromosom, Strand, exons, introns, transcripts);
                            aktuellesGen = newGene;
                        }

                        //kein neues Gen
                        else if(aktuellesGen.getGeneId().equals(gene_id)){
                            HashMap<String, ArrayList<Section>> exons = aktuellesGen.getExons();
                            //HashMap Exons erweitern:
                            //falls Protein schon vorhanden, nur noch Section hinzufügen
                            if (exons.containsKey(protein_id)) {
                                exons.get(protein_id).add(section);
                            }

                            //falls Protein noch nicht vorhanden -> Section und protID hinzufügen
                            else {
                                ArrayList<Section> firstSection = new ArrayList<>();
                                firstSection.add(section);
                                exons.put(protein_id, firstSection);
                            }

                            //Set transcript erweitern:
                            aktuellesGen.getTranscriptIDs().add(trancsript_id);
                        }
                        //es kommt ein neues Gen
                        else if(!aktuellesGen.getGeneId().equals(gene_id)) {
                            //SkippedExons berechnen

                            //Set zählen -> ntranscripts setzen
                            String ntransString = Integer.toString(aktuellesGen.getTranscriptIDs().size());
                            aktuellesGen.setNtrans(ntransString);

                            //nprots: Annahme: hier sind nicht die CDS Einträge gemeint sondern ein CDS sind alle Exons von einem Protein -> also ProteinID
                            String nprotsString = Integer.toString(aktuellesGen.getExons().size());
                            aktuellesGen.setNprots(nprotsString);

                            //Gene -> Introns berechnen -> Introns hinzufügen
                            //TODO: habe noch leeere Introns
                            aktuellesGen.calculateIntrons();

                            //TODO: Exon Skipping finden
                            //bei GeneInProgress, jedes Intron durchgehen
                            HashMap<String, ArrayList<Section>> Introns = aktuellesGen.getIntrons();
                            Iterator<HashMap.Entry<String, ArrayList<Section>>> itrSmall = Introns.entrySet().iterator();
                            HashSet<Section> actualSV = new HashSet<>();
                            while (itrSmall.hasNext()) {
                                String protId = itrSmall.next().getKey();
                                ArrayList<Section> possibleSVIntrons = Introns.get(protId);
                                for (int k = 0; k < possibleSVIntrons.size(); k++) {
                                    Section possibleSV = possibleSVIntrons.get(k);
                                    //damit man nicht die SV die man sowieso schon gemacht hat, nochmal macht
                                    if (!actualSV.contains(possibleSV)) {
                                        Object[] possibleSkipis = aktuellesGen.skippedExonBerechnen(possibleSV, protId, Introns);
                                        HashMap<String, HashSet<Section>> WTSE = (HashMap<String, HashSet<Section>>) possibleSkipis[0];
                                        if (!WTSE.isEmpty()) {
                                            SkippedExons actualSkippedExon = aktuellesGen.skippedExonErstellen(possibleSkipis, aktuellesGen.getExons(), possibleSV);
                                            String gi = aktuellesGen.genInfoAufbereitung();
                                            String zeile = ErgebnisSchreiben(gi, actualSkippedExon);
                                            writer.write(zeile);
                                            actualSV.add(possibleSV);
                                        }
                                    }
                                }
                            }
                            //neues Gen erstellen
                            String Chromosom = splitline[0];
                            String Strand = splitline[6];

                            //HashMap Exons erstellen
                            HashMap<String, ArrayList<Section>> exons = new HashMap<>();
                            ArrayList<Section> firstSectionEver = new ArrayList<>();
                            firstSectionEver.add(section);
                            exons.put(protein_id, firstSectionEver);

                            //HashMap Introns erstellen
                            HashMap<String, ArrayList<Section>> introns = new HashMap<>();

                            //Set mit transcript_ID erstellen
                            HashSet<String> transcripts = new HashSet<>();
                            transcripts.add(trancsript_id);

                            //Gen erstellen
                            Gene newGene = new Gene(gene_id, gene_symbol, Chromosom, Strand, exons, introns, transcripts);
                            aktuellesGen = newGene;

                        }
                    }
                }
            }
            //TODO: letztes Gen bzw skippedExons machen machen
            //SkippedExons berechnen

            //Set zählen -> ntranscripts setzen
            String ntransString = Integer.toString(aktuellesGen.getTranscriptIDs().size());
            aktuellesGen.setNtrans(ntransString);

            //nprots: Annahme: hier sind nicht die CDS Einträge gemeint sondern ein CDS sind alle Exons von einem Protein -> also ProteinID
            String nprotsString = Integer.toString(aktuellesGen.getExons().size());
            aktuellesGen.setNprots(nprotsString);

            //Gene -> Introns berechnen -> Introns hinzufügen
            //TODO: habe noch leeere Introns
            aktuellesGen.calculateIntrons();

            //TODO: Exon Skipping finden
            //bei GeneInProgress, jedes Intron durchgehen
            HashMap<String, ArrayList<Section>> Introns = aktuellesGen.getIntrons();
            Iterator<HashMap.Entry<String, ArrayList<Section>>> itrSmall = Introns.entrySet().iterator();
            HashSet<Section> actualSV = new HashSet<>();
            while (itrSmall.hasNext()) {
                String protId = itrSmall.next().getKey();
                ArrayList<Section> possibleSVIntrons = Introns.get(protId);
                for (int k = 0; k < possibleSVIntrons.size(); k++) {
                    Section possibleSV = possibleSVIntrons.get(k);
                    //damit man nicht die SV die man sowieso schon gemacht hat, nochmal macht
                    if (!actualSV.contains(possibleSV)) {
                        Object[] possibleSkipis = aktuellesGen.skippedExonBerechnen(possibleSV, protId, Introns);
                        HashMap<String, HashSet<Section>> WTSE = (HashMap<String, HashSet<Section>>) possibleSkipis[0];
                        if (!WTSE.isEmpty()) {
                            SkippedExons actualSkippedExon = aktuellesGen.skippedExonErstellen(possibleSkipis, aktuellesGen.getExons(), possibleSV);
                            String gi = aktuellesGen.genInfoAufbereitung();
                            String zeile = ErgebnisSchreiben(gi, actualSkippedExon);
                            writer.write(zeile);
                            actualSV.add(possibleSV);
                        }
                    }
                }
            }

            writer.close();
        }

    public static String ErgebnisSchreiben(String genInfo, SkippedExons skipi){
        String Ergebnis = "";
        String explizitInfo = skipi.toString();
        Ergebnis += genInfo + explizitInfo + "\n";
        return Ergebnis;
    }

}


