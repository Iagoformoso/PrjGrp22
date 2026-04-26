package com.sistema.datos;

import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.PosicionGPS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MaquinaDAO {
    private List<MaquinaExpendedora> maquinas = new ArrayList<>();

    public void addMaquinaExpendedora(MaquinaExpendedora maquina) {
        maquinas.add(maquina);
    }

    public List<MaquinaExpendedora> getAllMaquinas() {
        return maquinas;
    }

    public MaquinaExpendedora getMaquinaPorId(String id) {
        for (MaquinaExpendedora maquina : maquinas) {
            if(maquina.getIdMaquina().equals(id)) {
                return maquina;
            }
        }

        //Podría ser exception
        return null;
    }

    public void deleteMaquinaExpendedora(String id) {
        Iterator<MaquinaExpendedora> iterator = maquinas.iterator();
        while (iterator.hasNext()) {
            MaquinaExpendedora maquina = iterator.next();
            if(iterator.next().getIdMaquina().equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    public void modifyMaquina(MaquinaExpendedora maquinaActualizada) {
        for (int i = 0; i < maquinas.size(); i++) {
            if (maquinas.get(i).getIdMaquina().equals(maquinaActualizada.getIdMaquina())) {
                maquinas.set(i, maquinaActualizada);
            }
        }
    }

    public MaquinaExpendedora getMaquinaGPS(PosicionGPS gps) {
        for (MaquinaExpendedora maquina : maquinas) {
            if(maquina.getPosicionGPS().equals(gps)) {
                return maquina;
            }
        }

        //Podria ser exception
        return null;
    }
}
