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


            // ich habe jetzt einen BufferedReader der mir characters streamt. Jetzt muss ich einfach Zeile fuer Zeile durchgehen um mein Infos zu bekommen
            // ACHTUNG! man muss die line schon vorher erstellen und dann wi unten, sonst liest bufferedreader nur jede zweite zeile
            String line = null;
            while ((line = in.readLine()) != null) {
                //String line = in.readLine();
                // den String tab orientiert splitten
                if (line.startsWith("#") == false) {
                    String[] splitline = line.split("\t");
                    //man will nur die Zeilen speichern mit CDS
                    //Achtung: bei == ist nur Vergleich des Speicherorts und nicht die lexikalische Übereinstimmung -> man braucht equals
                    if (splitline[2].equals("CDS")) {
                        allNecessaryLines.add(splitline);
                    }
                }


            }

            //HasMap erstellen mit allen Infos zu den Genen
            HashMap <String, Gene> allGenes = new HashMap<>();

            for(int i = 0; i<allNecessaryLines.size(); i++) {
                String[] lines = allNecessaryLines.get(i);
                //alle notwendigen Spalten pro Zeile holen
                String Attribute = lines[8];
                String[] Attri = Attribute.split("; ");
                String protein_id = null;
                String gene_id = null;
                String gene_symbol = null;
                String trancsript_id = null;
                Section section = new Section(Integer.parseInt(lines[3]), Integer.parseInt(lines[4]));
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

                    }}


                    //nun alle Infos in die HashMap
                    //Fall 1: Gene ist bereits vorhanden -> nur noch erweitern
                    if (allGenes.containsKey(gene_id)) {
                        //TODO: verbraucht das zu viel Speicher?
                        Gene Genaktuell = allGenes.get(gene_id);
                        HashMap<String, ArrayList<Section>> exons = Genaktuell.getExons();
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
                        Genaktuell.getTranscriptIDs().add(trancsript_id);
                    }

                    //Fall 2: Gene ist noch nicht vorhanden
                    else {
                        //Array erstellen
                        String[] ersteGenInfo = new String[6];
                        ersteGenInfo[0] = gene_id;
                        ersteGenInfo[1] = gene_symbol;
                        ersteGenInfo[2] = lines[0];
                        ersteGenInfo[3] = lines[6];


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
                        Gene newGene = new Gene(ersteGenInfo, exons, introns, transcripts);


                        allGenes.put(gene_id, newGene);

                    }


                }
            //TODO: HashMap allGenes durchgehen
            HashMap<String, ArrayList<SkippedExons>> allSkippedExons= new HashMap<>();
            Iterator<HashMap.Entry<String, Gene>> itr = allGenes.entrySet().iterator();
            while(itr.hasNext()){
                Gene GeneInProgress = itr.next().getValue();

                //GenId
                String GenId = GeneInProgress.GenInfo[0];



                //Set zählen -> ntranscripts setzen
                String ntrans = Integer.toString(GeneInProgress.getTranscriptIDs().size());
                GeneInProgress.GenInfo[5] = ntrans;

                //nprots: Annahme: hier sind nicht die CDS Einträge gemeint sondern ein CDS sind alle Exons von einem Protein -> also ProteinID
                String nprots = Integer.toString(GeneInProgress.getExons().size());
                GeneInProgress.GenInfo[4] = nprots;

                //Gene -> Introns berechnen -> Introns hinzufügen
                //TODO: habe noch leeere Introns
                GeneInProgress.setIntrons(GeneInProgress.calculateIntrons(GeneInProgress.getExons()));

                //TODO: Exon Skipping finden
                //bei GeneInProgress, jedes Intron durchgehen
                HashMap<String, ArrayList<Section>> Introns = GeneInProgress.getIntrons();
                ArrayList<SkippedExons> skippedExonsGen = new ArrayList<>();
                Iterator<HashMap.Entry<String, ArrayList<Section>>> itrSmall = Introns.entrySet().iterator();
                HashSet<Section> actualSV = new HashSet<>();
                while(itrSmall.hasNext()){
                    String protId = itrSmall.next().getKey();
                    ArrayList<Section> possibleSVIntrons = Introns.get(protId);
                    for(int k = 0; k<possibleSVIntrons.size(); k++){
                        Section possibleSV = possibleSVIntrons.get(k);
                        //damit man nicht die SV die man sowieso schon gemacht hat, nochmal macht
                        if(!actualSV.contains(possibleSV)){
                        Object[] possibleSkipis = GeneInProgress.skippedExonBerechnen(possibleSV, protId,Introns);
                        HashMap<String,HashSet<Section>> WTSE = (HashMap<String, HashSet<Section>>) possibleSkipis[0];
                        if(!WTSE.isEmpty()){
                            SkippedExons actualSkippedExon = GeneInProgress.skippedExonErstellen(possibleSkipis, GeneInProgress.getExons(), possibleSV);

                                skippedExonsGen.add(actualSkippedExon);


                            actualSV.add(possibleSV);
                        }
                        }
                    }
                }
                if(!skippedExonsGen.isEmpty()){
                    allSkippedExons.put(GenId, skippedExonsGen);
                }
            }

           /* Iterator<HashMap.Entry<String, ArrayList<SkippedExons>>> iti = allSkippedExons.entrySet().iterator();
            int zähler = 0;
            //jetzt schaue ich ein WTGen an
            while (iti.hasNext()) {
                String gen = iti.next().getKey();
                int zahl = allSkippedExons.get(gen).size();
                ArrayList<SkippedExons> sk = allSkippedExons.get(gen);
                sk.stream().forEach(ex -> System.out.println(ex));
                System.out.println(zahl);
            }*/



            //TODO: Das Ergebnis drucken in eine File
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputpath));
            //Überschrift
            writer.write("id\tsymbol\tchr\tstrand\tnprots\tntrans\tSV\tWT\tWT_prots\tSV_prots\tmin_skipped_exon\tmax_skipped_exon\tmin_skipped_bases\tmax_skipped_bases\n");

            for(String key : allSkippedExons.keySet()){
                String[] generalInfo = allGenes.get(key).getGenInfo();
                String genInfo = allGenes.get(key).genInfoAufbereitung(generalInfo);
                ArrayList<SkippedExons> explizitSkippedExon = allSkippedExons.get(key);
                for(int i = 0; i<explizitSkippedExon.size(); i++){
                    String alles = "";
                    SkippedExons skipi = explizitSkippedExon.get(i);
                    String explizitInfo = skipi.toString();
                    alles += genInfo + explizitInfo;
                    writer.write(alles + "\n");

                }




            }

            writer.close();

            //TODO: Semikolons noch entfernen



        }
}


