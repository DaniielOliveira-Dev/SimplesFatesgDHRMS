package com.fatesg.web_api.apis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.fatesg.biblioteca.dtos.FolhaDto;
import com.fatesg.biblioteca.dtos.ReciboDto;
import com.fatesg.biblioteca.interfaces.ClienteDeCalculoFolhaInterface;
import com.fatesg.biblioteca.protocolos.jsonrpc.JsonRpcRequest;
import com.fatesg.biblioteca.protocolos.jsonrpc.JsonRpcResponse;
import com.fatesg.web_api.configs.JsonRPCConfig;
import com.google.gson.Gson;

public class ServidorDeCalculoApi implements ClienteDeCalculoFolhaInterface {
    private Socket cliente;
    private PrintWriter saida;
    private BufferedReader entrada;

    // controla alternância entre os dois servidores
    private static boolean usarPrimeiroServidor = true;

    public ServidorDeCalculoApi() {
        super();
    }

    private void conectar() throws IOException {
        short porta;

        // alterna entre os dois servidores configurados
        if (usarPrimeiroServidor) {
            porta = JsonRPCConfig.JSON_RPC_SERVER_PORT;
        } else {
            porta = JsonRPCConfig.JSON_RPC_SERVER_PORT2;
        }

        // inverte para a próxima requisição
        usarPrimeiroServidor = !usarPrimeiroServidor;

        this.cliente = new Socket(JsonRPCConfig.JSON_RPC_SERVER_HOST, porta);
        this.saida = new PrintWriter(cliente.getOutputStream(), true);
        this.entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
    }

    private void fecharConexao() throws IOException {
        if (cliente != null && cliente.isConnected()) {
            saida.close();
            entrada.close();
            cliente.close();
            cliente = null;
        }
    }

    private JsonRpcResponse<Object> enviar(JsonRpcRequest request) throws IOException, Exception {
        conectar();

        if (!cliente.isConnected())
            throw new IOException("Socket cliente não está conectado!");

        String reqJson = new Gson().toJson(request);
        this.saida.println(reqJson);

        String resJson = "";
        String retorno;

        while ((retorno = this.entrada.readLine()) != null) {
            resJson += retorno;
        }

        @SuppressWarnings("unchecked")
        JsonRpcResponse<Object> response =
                (JsonRpcResponse<Object>) new Gson().fromJson(resJson, JsonRpcResponse.class);

        fecharConexao();

        return response;
    }

    @Override
    public FolhaDto calcularFolhaDePagamento(byte mes, short ano) {
        try {
            JsonRpcRequest request = new JsonRpcRequest();
            request.id = 1;
            request.method = "calcularFolhaDePagamento";
            request.params = new String[] {
                    String.valueOf(mes),
                    String.valueOf(ano)
            };

            JsonRpcResponse<Object> response = enviar(request);

            if (response.error != null) {
                throw new Exception(response.error.message);
            }

            if (response.result == null) {
                throw new Exception("Resultado nulo");
            }

            var parser = new Gson();
            String retorno = parser.toJson(response.result);

            return parser.fromJson(retorno, FolhaDto.class);

        } catch (IOException e) {
            System.err.println("Erro de IOException no calcularFolhaDePagamento: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Erro no calcularFolhaDePagamento: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FolhaDto calcularFolhaDePagamentoDoDepartamento(String arg0, byte arg1, short arg2) {
        throw new UnsupportedOperationException("Unimplemented method 'calcularFolhaDePagamentoDoDepartamento'");
    }

    @Override
    public ReciboDto calcularReciboDePagamento(int idFuncionario, byte mes, short ano) {
        try {
            JsonRpcRequest request = new JsonRpcRequest();
            request.id = 2;
            request.method = "calcularReciboDePagamento";
            request.params = new String[] {
                    String.valueOf(idFuncionario),
                    String.valueOf(mes),
                    String.valueOf(ano)
            };

            JsonRpcResponse<Object> response = this.enviar(request);

            if (response.error != null) {
                throw new Exception(response.error.message);
            }

            if (response.result == null) {
                throw new Exception("Resultado nulo!");
            }

            var parser = new Gson();
            String retorno = parser.toJson(response.result);

            return parser.fromJson(retorno, ReciboDto.class);

        } catch (IOException e) {
            System.err.println("Erro de IOException no calcularReciboDePagamento: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Erro no calcularReciboDePagamento: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public double calcularSalarioLiquido(double salarioBruto) {
        try {
            JsonRpcRequest request = new JsonRpcRequest();
            request.id = 2;
            request.method = "calcularSalarioLiquido";
            request.params = new String[] { String.valueOf(salarioBruto) };

            JsonRpcResponse<Object> response = this.enviar(request);

            if (response.error != null) {
                throw new Exception(response.error.message);
            }

            Double retorno = (Double) response.result;

            return retorno;

        } catch (IOException e) {
            System.err.println("Erro de IOException no calcularSalarioLiquido: " + e.getMessage());
            e.printStackTrace();
            return 0.00;
        } catch (Exception e) {
            System.err.println("Erro no calcularSalarioLiquido: " + e.getMessage());
            e.printStackTrace();
            return 0.00;
        }
    }

}
