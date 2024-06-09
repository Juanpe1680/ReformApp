package com.example.reformapp.Models;

public class FechasEmpresaModel {
    private long fechaIni;
    private long fechaFin;

    public FechasEmpresaModel(long fechaIni, long fechaFin) {
        this.fechaIni = fechaIni;
        this.fechaFin = fechaFin;
    }

    public long getFechaIni() {
        return fechaIni;
    }

    public void setFechaIni(long fechaIni) {
        this.fechaIni = fechaIni;
    }

    public long getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(long fechaFin) {
        this.fechaFin = fechaFin;
    }
}
