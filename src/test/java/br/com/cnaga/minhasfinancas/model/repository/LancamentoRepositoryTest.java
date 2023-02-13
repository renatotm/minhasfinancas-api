package br.com.cnaga.minhasfinancas.model.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.cnaga.minhasfinancas.model.entity.Lancamento;
import br.com.cnaga.minhasfinancas.model.enums.StatusLancamento;
import br.com.cnaga.minhasfinancas.model.enums.TipoLancamento;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoRepositoryTest {

	@Autowired
	LancamentoRepository repository;
	@Autowired 
	TestEntityManager entityManager;
	
	@Test
	public void deveSalvarUmLancamento() {
		Lancamento lancamento = criarLancamento();
		lancamento = repository.save(lancamento);
		
		Assertions.assertNotNull(lancamento.getId());
	}

	@Test
	public void deveDeletarUmLancamento() {
		Lancamento lancamento = criarEPersistirUmLancamento();
		
		entityManager.find(Lancamento.class, lancamento.getId());
		
		repository.delete(lancamento);
		
		Lancamento lancamentoApagado = entityManager.find(Lancamento.class, lancamento.getId());
		
		Assertions.assertNull(lancamentoApagado);
	}

	@Test
	public void deveAtualizarUmLancamento() {

		Lancamento lancamento = criarEPersistirUmLancamento();
		
		lancamento.setAno(2024);
		lancamento.setDescricao("Descricao alterada");
		
		repository.save(lancamento);
		
		Lancamento lancamentoAtualizado = entityManager.find(Lancamento.class, lancamento.getId());
		
		Assertions.assertEquals(lancamentoAtualizado.getAno(), 2024);
		Assertions.assertEquals(lancamentoAtualizado.getDescricao(), "Descricao alterada");
	}	
	
	@Test
	public void deveBuscarUmLancamentoPorId() {
		Lancamento lancamento = criarEPersistirUmLancamento();
		
		Optional<Lancamento> lancamentoEncontrado = repository.findById(lancamento.getId());
		
		Assertions.assertTrue(lancamentoEncontrado.isPresent());
	}
	
	private Lancamento criarEPersistirUmLancamento() {
		Lancamento lancamento = criarLancamento();
		lancamento = entityManager.persist(lancamento);
		return lancamento;
	}
	
	public static Lancamento criarLancamento() {
		return Lancamento.builder()
				.ano(2023)
				.mes(2)
				.descricao("Lancamento")
				.valor(BigDecimal.valueOf(10.5))
				.tipo(TipoLancamento.RECEITA)
				.status(StatusLancamento.PENDENTE)
				.dataCadastro(LocalDate.now())
				.build();
	}
}
