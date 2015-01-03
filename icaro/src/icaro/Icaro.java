package icaro;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Panama Hitek Creative Team
 * @see <a href="http://panamahitek.com">http://panamahitek.com</a>
 * @version 1.0.0 02 de enero de 2015
 */
public class Icaro {

    /**
     * OutputStream para el envío de datos por el Puerto Serie
     */
    private static OutputStream Output = null;
    /**
     * Variable que representa el Puerto Serie
     */
    private static SerialPort serialPort;

    /**
     * Variables con los parámetros por defecto del Puerto Serie.
     */
    private static int ByteSize = 8;
    private static int StopBits = 1;
    private static int Parity = 0;
    private static int TimeOut = 2000;
    /**
     * Variable flag que representa el estado del Puerto Serie
     */
    private static boolean portOpen = false;

    /**
     * Método para establecer la paridad en la conexión con el Puerto Serie. La
     * paridad por defecto es "Sin Paridad"
     *
     * @param input_Parity <br>0 = Sin Paridad <br>1 = Paridad Impar <br>2 =
     * Paridad Par <br>3 = Paridad Marcada <br>4 = Paridad Espaciada
     */
    public void setParity(int input_Parity) {

        if ((input_Parity >= 0) && (input_Parity <= 4)) {
            Parity = input_Parity;
        } else {
            Parity = 0;
            System.out.println("La paridad solamente puede ser: \n"
                    + "0 = Sin Paridad\n"
                    + "1 = Paridad Impar\n"
                    + "2 = Paridad Par\n"
                    + "3 = Paridad Marcada\n"
                    + "4 = Paridad Espaciada\n"
                    + "Se conserva la paridad por defecto (0- Sin Paridad)");
        }
    }

    /**
     * Método para establecer el ByteSize Se aceptan valores de entrada entre 5
     * y 8.
     *
     * @param Bytes Valor tipo entero para establecer el ByteSize
     */
    public void setByteSize(int Bytes) {

        if ((Bytes >= 5) && (Bytes <= 8)) {
            ByteSize = Bytes;
        } else {
            ByteSize = 8;
            System.out.println("Sólo se aceptan valores entre 5 y 8 para el ByteSize "
                    + "\nSe conserva el valor por defecto (8 Bytes)");
        }
    }

    /**
     * Método para establecer el StopBit
     *
     * @param Bits Se establecen los StopBits
     * <br> 1 = 1 StopBit
     * <br> 2 = 2 StopBits
     * <br> 3 = 1.5 StopBits
     */
    public void setStopBits(int Bits) {

        if ((Bits >= 1) && (Bits <= 3)) {
            StopBits = Bits;
        } else {
            StopBits = 1;
            System.out.println("Sólo se aceptan valores entre 1 y 3 para StopBit (3 es para 1.5 StopBits)."
                    + "\nSe conserva el valor por defecto (1 Bit)");
        }
    }

    /**
     * Método para establecer el TimeOut
     *
     * @param time
     * <br> Valor tipo entero, dado en milisegundos
     */
    public void setTimeOut(int time) {
        TimeOut = time;
    }

    /**
     * Método para abrir el Puerto Serie. Si no se establecen los parámetros
     * para la conexión (TimeOut, ByteSize, StopBits, Parity) el algoritmo
     * tomará los valores establecidos por defecto. Estos son:
     * <br>ByteSize = 8;
     * <br>StopBits = 1
     * <br>Parity = 0 (Sin Paridad)
     * <br>TimeOut = 2000;
     *
     * @param PORT_NAME String con el nombre del puerto, el cual tiene el
     * formato COM seguido del número del puerto asignado a determinado
     * dispositivo
     *
     * @param DATA_RATE La velocidad de transmisión de datos, dada en baudios
     * por segundo
     *
     * @throws Exception Se puede dar el caso de que el puerto serie esté en
     * uso, o que ya se haya abierto el puerto y se intente abrir nuevamente.
     *
     * @see setParity(int Parity)
     * <br>setByteSize(int Bytes)
     * <br>setStopBits(int Bits)
     * <br>setTimeOut(int time)
     */
    public void Iniciar(String PORT_NAME, int DATA_RATE) throws Exception {
        /*
         El flag portOpen es el encargado de evitar que se intente abrir el puerto 2 veces.
         */
        if (!portOpen) {
            try {
                CommPortIdentifier portId = null;
                Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

                while (portEnum.hasMoreElements()) {
                    CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
                    if (PORT_NAME.equals(currPortId.getName())) {
                        portId = currPortId;
                        break;
                    }
                }
                serialPort = (SerialPort) portId.open(this.getClass().getName(), TimeOut);
                serialPort.setSerialPortParams(DATA_RATE, ByteSize, StopBits, Parity);

                //Se establece la variable para enviar los datos a través del Puerto Serie.
                Output = serialPort.getOutputStream();

                //Se establece el valor del flag portOpen como true, indicando que se ha iniciado la conexión
                //con el puerto Serie
                portOpen = true;
                System.out.println("Se ha iniciado la conexión con el Puerto Serie");
            } catch (IOException ex) {
                Logger.getLogger(Icaro.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedCommOperationException ex) {
                throw new Exception("Operación no permitida.");
            } catch (PortInUseException ex) {
                throw new Exception("El puerto seleccionado ya está en uso.");
            }
        } else {
            throw new Exception("El Puerto Serie ya ha sido abierto. Imposible abrir 2 veces.");
        }
    }

    /**
     * Método para finalizar la conexión con el Puerto Serie.
     *
     * @throws Exception Se puede dar el caso que se intente cerrar el Puerto
     * Serie sin que éste esté abierto, para lo cual se lanzará una excepción.
     */
    public void Cerrar() throws Exception {
        if (!portOpen) {
            serialPort.close();
            portOpen = false;
            System.out.println("Se ha finalizado la conexión con el Puerto Serie");
        } else {
            throw new Exception("El Puerto Serie no se ha abierto. Imposible Cerrar");
        }
    }

    private void sendData(String inputData) {
        try {
            Output.write(inputData.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Icaro.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void Activar(int Valor) throws Exception {
        if (portOpen) {
            sendData("s" + (char) Valor);
        } else {
            throw new Exception("La placa no se puede iniciar debido a que no se ha abierto el puerto serie.");
        }
    }

    public int LeerValorAnalogico(int Sensor) {
        int Output = 0;

    }

    public void LeerValorDigital(int Sensor) {

        if ((Sensor < 4) && (Sensor > 1)) {
            sendData("d" + (char) Sensor);

            /* Aqui falta codigo */
        } else {
            System.out.println("El valor del sensor solo puede estar entre un rango de 1 a 4.");
        }
    }

    public void ActivarServo(int Servo, int Valor) {
        if (portOpen) {
            sendData("m");
            if (Servo == 1) {
                sendData("1");
            } else if (Servo == 2) {
                sendData("2");
            } else if (Servo == 3) {
                sendData("3");
            } else if (Servo == 4) {
                sendData("4");
            } else if (Servo == 5) {
                sendData("5" + (char) Valor);
            }
        }
    }

    public Sonido(int Audio, int ValorPuerto) {
        if (porOpen) {
            sendData("a" + (char) Audio + (char) ValorPuerto );
            sendData("s" + (char) 0 ); 
        }
    }

}
