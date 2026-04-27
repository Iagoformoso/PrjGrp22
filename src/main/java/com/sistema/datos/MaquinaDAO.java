package com.sistema.datos;

import com.sistema.excepciones.*;
import com.sistema.modelo.entidades.MaquinaExpendedora;
import com.sistema.modelo.entidades.PosicionGPS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MaquinaDAO {
    private List<MaquinaExpendedora> maquinas = new ArrayList<>();

    public void addMaquinaExpendedora(MaquinaExpendedora maquina) throws OperacionNoExitosa {
    	try {
    		// Se intenta recibir una máquina "duplicada" con el mismo id que la máquina a insertar
    		MaquinaExpendedora duplicado = this.getMaquinaPorId(maquina.getIdMaquina());
    		// Si no se ha saltado a la sentencia catch, existe un duplicado, y lanzamos una excepción
    		throw new OperacionNoExitosa("Ya existe una máquina expendedora con el mismo identificador.");
    	} catch (MaquinaNoEncontrada one) {
    		// Si salta una excepcion, ergo, no hay una máquina con el mismo id, se introduce
    		maquinas.add(maquina);
    	}
    }

    public List<MaquinaExpendedora> getAllMaquinas(){
    	// Se está vacía deberíase mandar unha excepción ou enténdese que pode ser unha situación normal?
        return maquinas;
    }

    public MaquinaExpendedora getMaquinaPorId(String id) throws MaquinaNoEncontrada{
        for (MaquinaExpendedora maquina : maquinas) {
            if(maquina.getIdMaquina().equals(id)) {
                return maquina;
            }
        }
        throw new MaquinaNoEncontrada("No se ha encontrado ninguna máquina expendedora con ese identificador.");
    }
    
    public MaquinaExpendedora getMaquinaGPS(PosicionGPS gps) throws MaquinaNoEncontrada {
        for (MaquinaExpendedora maquina : maquinas) {
            if(maquina.getPosicionGPS().equals(gps)) {
                return maquina;
            }
        }
        throw new MaquinaNoEncontrada("No se ha encontrado ninguna máquina expendedora con esa posición gps.");
    }

    public void deleteMaquinaExpendedora(String id) throws MaquinaNoEncontrada {
        Iterator<MaquinaExpendedora> iterator = maquinas.iterator();
        while (iterator.hasNext()) {
            MaquinaExpendedora maquina = iterator.next();
            if(iterator.next().getIdMaquina().equals(id)) {
                iterator.remove();
                return;
            }
        }
        throw new MaquinaNoEncontrada("No se ha encontrado ninguna máquina expendedora para eliminar.");
    }

    public void modifyMaquina(MaquinaExpendedora maquinaActualizada) throws MaquinaNoEncontrada {
    	// Se ao listar se pasa a referencia da máquina, e o usuario a modifica, non estaría xa modificada na lista de máquinas?
    	// É dicir, non sería innecesario esto, a menos que cando se devolva unha máquina se mande unha copia en lugar da orixinal?
        for (int i = 0; i < maquinas.size(); i++) {
            if (maquinas.get(i).getIdMaquina().equals(maquinaActualizada.getIdMaquina())) {
                maquinas.set(i, maquinaActualizada);
                return;
            }
        }
        throw new MaquinaNoEncontrada("No se ha encontrado la máquina expendedora para modificar.");
    }


}
