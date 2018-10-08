package com.check.face.facecheck.Models;

public class Face {
    private int Codigo;
    private Float SmilingProbability;
    private Float RightEyeOpenProbability;
    private Float LeftEyeOpenProbability;
    private Float NoseX;
    private Float NoseY;

    public Face() {
    }

    public Integer getSmilingProbability() {
        int retorno = Integer.parseInt((""+SmilingProbability).replace(".",",").split(",")[0]);
        retorno = retorno < 0 ? 0 : retorno;
        return retorno;
    }

    public void setSmilingProbability(Float smilingProbability) {
        SmilingProbability = smilingProbability;
    }

    public Integer getRightEyeOpenProbability() {
        return Integer.parseInt((""+RightEyeOpenProbability).replace(".",",").split(",")[0]);
    }

    public void setRightEyeOpenProbability(Float rightEyeOpenProbability) {
        RightEyeOpenProbability = rightEyeOpenProbability;
    }

    public Integer getLeftEyeOpenProbability() {
        return Integer.parseInt((""+LeftEyeOpenProbability).replace(".",",").split(",")[0]);
    }

    public void setLeftEyeOpenProbability(Float leftEyeOpenProbability) {
        LeftEyeOpenProbability = leftEyeOpenProbability;
    }

    public Float getNoseX() {
        return NoseX;
    }

    public void setNoseX(Float noseX) {
        NoseX = noseX;
    }

    public Float getNoseY() {
        return NoseY;
    }

    public void setNoseY(Float noseY) {
        NoseY = noseY;
    }

    public int getCodigo() {
        return Codigo;
    }

    public void setCodigo(int codigo) {
        Codigo = codigo;
    }

    @Override
    public String toString() {
        return "Face{" +
                "Codigo=" + Codigo +
                ", SmilingProbability=" + getSmilingProbability() +
                "%, RightEyeOpenProbability=" + getRightEyeOpenProbability() +
                "%, LeftEyeOpenProbability=" + getLeftEyeOpenProbability() +
                "%, NoseX=" + getNoseX() +
                ", NoseY=" + getNoseY() +
                '}';
    }
}
