(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .controller('MainCtrl', MainCtrl);

  MainCtrl.$inject = ['Fornecedores'];

  /*jshint latedef: nofunc */
  function MainCtrl(Fornecedores) {
    var vm = this;
    vm.fornecedores = [{
      "id":"a15fae38-ee76-442d-a685-f074f6e186b4",
      "cnpjCpf":"020e0c59-e0e6-475c-847b-31c53e0a4423",
      "nome":"Super Dragon Ball Z",
      "atividadeEconomica":"Topa Tudo",
      "anoInicial":2008,
      "anoFinal":2016,
      "qtdLicitacoes":25,"valorTotal":0.36394552346549347,
      "fidelidade":[{
        "nomePartido":"PMDB",
        "indice":223,
        "qtdLicitacoes":653,
        "valorTotal":0.1021952290702145},
        {"nomePartido":"PT","indice":285,"qtdLicitacoes":744,"valorTotal":0.6082507497416456},{"nomePartido":"PSDB","indice":725,"qtdLicitacoes":391,"valorTotal":0.5247817741432019}]},{"id":"472cc378-2636-4a73-bcdf-1e154e405402","cnpjCpf":"0eb970a8-2fcc-4075-8968-05b3f93f9612","nome":"Pokemon Go","atividadeEconomica":"Topa Tudo","anoInicial":2008,"anoFinal":2016,"qtdLicitacoes":896,"valorTotal":0.8905280970934502,"fidelidade":[{"nomePartido":"PMDB","indice":850,"qtdLicitacoes":707,"valorTotal":0.05732919406034187},{"nomePartido":"PT","indice":165,"qtdLicitacoes":577,"valorTotal":0.7692096996588554},{"nomePartido":"PSDB","indice":655,"qtdLicitacoes":287,"valorTotal":0.8832516677593356}]},{"id":"b4fe496a-6e0e-4cb8-9c85-847f36b5b33e","cnpjCpf":"1dd6f10a-aab7-459a-9b9c-183f75b7477e","nome":"Oh My!","atividadeEconomica":"Topa Tudo","anoInicial":2008,"anoFinal":2016,"qtdLicitacoes":807,"valorTotal":0.6934538397457437,"fidelidade":[{"nomePartido":"PMDB","indice":261,"qtdLicitacoes":896,"valorTotal":0.7757001177195011},{"nomePartido":"PT","indice":739,"qtdLicitacoes":325,"valorTotal":0.29379047000834113},{"nomePartido":"PSDB","indice":654,"qtdLicitacoes":542,"valorTotal":0.5622915689116277}]},{"id":"dbbf148c-73b0-4473-b25f-e25f62656ca1","cnpjCpf":"a04a3fe4-9b3c-41d3-958b-2683d7ba80b0","nome":"Ghostbusters","atividadeEconomica":"Topa Tudo","anoInicial":2008,"anoFinal":2016,"qtdLicitacoes":111,"valorTotal":0.23993154921445126,"fidelidade":[{"nomePartido":"PMDB","indice":624,"qtdLicitacoes":971,"valorTotal":0.09012881821001617},{"nomePartido":"PT","indice":3,"qtdLicitacoes":908,"valorTotal":0.17324015302655305},{"nomePartido":"PSDB","indice":350,"qtdLicitacoes":488,"valorTotal":0.21119180635280654}]},{"id":"d01afd78-26e4-49d0-af28-181ae076766b","cnpjCpf":"fdeeb969-6463-4924-a013-5409476a370b","nome":"Blablabla","atividadeEconomica":"Topa Tudo","anoInicial":2008,"anoFinal":2016,"qtdLicitacoes":727,"valorTotal":0.7160029217962143,"fidelidade":[{"nomePartido":"PMDB","indice":842,"qtdLicitacoes":828,"valorTotal":0.9996686656875406},{"nomePartido":"PT","indice":741,"qtdLicitacoes":283,"valorTotal":0.37623504777255257},{"nomePartido":"PSDB","indice":404,"qtdLicitacoes":342,"valorTotal":0.7089893439668818}]}];

    // vm.fornecedores = Fornecedores.query();
  }
})();
