package com.fatesg.servicos;

import java.util.HashMap;
import java.util.List;

import com.fatesg.apis.ServidorDeDadosSalarioApi;
import com.fatesg.biblioteca.dtos.FolhaDto;
import com.fatesg.biblioteca.dtos.ReciboDto;
import com.fatesg.biblioteca.dtos.SalarioDto;
import com.fatesg.biblioteca.interfaces.ServidorDeCalculoFolhaInterface;

public class CalculoDeFolhaService implements ServidorDeCalculoFolhaInterface {
    private ServidorDeDadosSalarioApi stub;

    public CalculoDeFolhaService() {
        this.stub = new ServidorDeDadosSalarioApi();
        this.stub.Conectar();
    }

    @Override
    public FolhaDto calcularFolhaDePagamento(byte mes, short ano, HashMap<String, Double> descontos) {
        try {
            FolhaDto folha = new FolhaDto(mes, ano);
            int offset = 0;
            int limit = 50;
            List<SalarioDto> salarios;

            salarios = stub.listarSalarios(limit, offset);

            for (SalarioDto salarioDto : salarios) {
                double salarioBruto = salarioDto.getValor() / 12;

                ReciboDto recibo = new ReciboDto(
                        mes,
                        ano,
                        salarioDto.getIdFuncionario(),
                        new SalarioDto(salarioDto.getIdFuncionario(), salarioBruto));

                // aplica os descontos configurados no recibo
                descontos.forEach((k, v) -> {
                    recibo.addDesconto(k, v);
                });

                double salarioLiquido = calcularSalarioLiquido(salarioBruto, descontos);
                recibo.setSalarioLiquido(salarioLiquido);

                folha.addRecibo(recibo);
            }

            return folha;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ReciboDto calcularReciboDePagamento(int idFuncionario, byte mesReferencia, short anoReferencia,
            HashMap<String, Double> descontos) {
        try {
            SalarioDto salarioBrutoAnual = stub.obterSalarioPorId(idFuncionario);
            if (salarioBrutoAnual == null)
                throw new Exception("Salario não encontrado para o ID:" + idFuncionario);

            double salarioBruto = salarioBrutoAnual.getValor() / 12;
            var recibo = new ReciboDto(
                    mesReferencia,
                    anoReferencia,
                    idFuncionario,
                    new SalarioDto(idFuncionario, salarioBruto));

            descontos.forEach((k, v) -> {
                recibo.addDesconto(k, v);
            });

            double salarioLiquido = calcularSalarioLiquido(salarioBruto, descontos);
            recibo.setSalarioLiquido(salarioLiquido);

            return recibo;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public double calcularSalarioLiquido(double salarioBruto, HashMap<String, Double> descontos) {
        var salarioLiquido = salarioBruto;
        for (var desconto : descontos.values()) {
            var valorDesconto = salarioBruto * (desconto / 100d);
            salarioLiquido -= valorDesconto;
        }
        return salarioLiquido;
    }

    @Override
    public FolhaDto calcularFolhaDePagamentoDoDepartamento(String arg0, byte arg1, short arg2,
            HashMap<String, Double> arg3) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calcularFolhaDePagamentoDoDepartamento'");
    }

}
