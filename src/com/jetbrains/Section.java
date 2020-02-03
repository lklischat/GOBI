package com.jetbrains;

import java.util.Objects;

public class Section implements Comparable {
   private int beginning;
   private int ending;

    public Section(int beginning, int ending) {
        this.beginning = beginning;
        this.ending = ending;
    }

    public int getBeginning() {
        return beginning;
    }



    public int getEnding() {
        return ending;
    }


    @Override
    public int compareTo(Object o) {
        Section secti = (Section) o;
        if(this.getEnding()<secti.getBeginning()){
            return -1;
        }
        else if (this.getEnding()>secti.getBeginning()){
            return 1;
        }
        else{
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        Section section = (Section) o;
        if (this == section) return true;

        if (section == null || this.beginning != section.getBeginning() || this.ending != section.getEnding()) return false;

        return getBeginning() == section.getBeginning() &&
                getEnding() == section.getEnding();
    }



    @Override
    public String toString() {
        return beginning + ":" + ending;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBeginning(), getEnding());
    }
}

