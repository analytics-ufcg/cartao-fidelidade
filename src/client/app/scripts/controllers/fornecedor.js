(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .controller('FornecedorCtrl', FornecedorCtrl);

  FornecedorCtrl.$inject = ['Fornecedores'];

  /*jshint latedef: nofunc */
  function FornecedorCtrl(Fornecedores) {
    var vm = this;
    vm.fornecedor = {
      "id":"a15fae38-ee76-442d-a685-f074f6e186b4",
      "cnpjCpf":"020e0c59-e0e6-475c-847b-31c53e0a4423",
      "nome":"Super Dragon Ball Z",
      "atividadeEconomica":"Topa Tudo",
      "anoInicial":2008,
      "anoFinal":2016,
      "qtdLicitacoes":25,"valorTotal":0.36394552346549347,
      "fidelidade":[
        {"nomePartido":"PMDB", "indice":223,"qtdLicitacoes":653,"valorTotal":0.1021952290702145},
        {"nomePartido":"PT","indice":285,"qtdLicitacoes":744,"valorTotal":0.6082507497416456},
        {"nomePartido":"PSDB","indice":725,"qtdLicitacoes":391,"valorTotal":0.5247817741432019}
      ]
      };

    // vm.fornecedores = Fornecedores.get({id: xxx});
  }
})();
