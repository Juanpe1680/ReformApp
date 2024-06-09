package com.example.reformapp.Utils;

import com.google.android.material.datepicker.CalendarConstraints;

import java.util.ArrayList;

public class BloqueadorDias implements CalendarConstraints.DateValidator {
    // AL con las fechas (en milisegundos) que van a ser bloqueadas
    private final ArrayList<Long> diasBloquear;

    public BloqueadorDias(ArrayList<Long> blockedDays) {
        this.diasBloquear = blockedDays;
    }

    /* Metodo para verificar si una fecha no está en la lista de fechas bloqueadas 'diasBloquear'.
     * Devuelve true si la fecha es valida (no está bloqueada) y false si está bloqueada.*/
    @Override
    public boolean isValid(long date) {
        return !diasBloquear.contains(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeList(diasBloquear);
    }

    // El objeto ParcelableCreator genera una instancia de la clase BloqueadorDías
    public static final android.os.Parcelable.Creator<BloqueadorDias> CREATOR = new android.os.Parcelable.Creator<BloqueadorDias>() {
        // El Parcel es para empaquetar datos y mandarlos por la aplicacion de Android
        @Override
        public BloqueadorDias createFromParcel(android.os.Parcel source) {
            ArrayList<Long> diasBloqueados = new ArrayList<>();
            source.readList(diasBloqueados, Long.class.getClassLoader());
            return new BloqueadorDias(diasBloqueados);
        }

        @Override
        public BloqueadorDias[] newArray(int size) {
            return new BloqueadorDias[size];
        }
    };
}
