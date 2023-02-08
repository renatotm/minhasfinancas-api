package br.com.cnaga.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.cnaga.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{

}
