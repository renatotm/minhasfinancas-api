package br.com.cnaga.minhasfinancas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.cnaga.minhasfinancas.exception.RegraNegocioException;
import br.com.cnaga.minhasfinancas.model.entity.Lancamento;
import br.com.cnaga.minhasfinancas.model.entity.Usuario;
import br.com.cnaga.minhasfinancas.model.enums.StatusLancamento;
import br.com.cnaga.minhasfinancas.model.enums.TipoLancamento;
import br.com.cnaga.minhasfinancas.model.repository.LancamentoRepository;
import br.com.cnaga.minhasfinancas.model.repository.LancamentoRepositoryTest;
import br.com.cnaga.minhasfinancas.service.impl.LancamentoServiceImpl;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

	@SpyBean
	LancamentoServiceImpl service;
	@MockBean
	LancamentoRepository repository;
	
	@Test
	public void deveSalvarUmLancamento() {
		//cenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doNothing().when(service).validar(lancamentoASalvar);
		
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1L);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		
		//execucao
		Lancamento lancamento = service.salvar(lancamentoASalvar);
		
		//verificacao
		Assertions.assertEquals(lancamento.getId(), lancamentoSalvo.getId());
		Assertions.assertEquals(lancamento.getStatus(), StatusLancamento.PENDENTE);
	}
	
	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		//cenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);
		
		//execucao e verificacao
		Assertions.assertTrue(catchThrowableOfType(() -> service.salvar(lancamentoASalvar), RegraNegocioException.class) != null);
		
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);

	}
	
	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
		//cenario
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		
		//execucao e verificacao
		Assertions.assertTrue(catchThrowableOfType(() -> service.atualizar(lancamentoASalvar), NullPointerException.class) != null);
		
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);

	}	
	
	@Test
	public void deveAtualizarUmLancamento() {
		//cenario
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1L);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		
		Mockito.doNothing().when(service).validar(lancamentoSalvo);
		
		Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		
		//execucao
		service.atualizar(lancamentoSalvo);
		
		//verificacao
		Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
	}	
	
	@Test
	public void deveDeletarUmLancamento() {
		//cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);

		//execucao
		service.deletar(lancamento);
		
		//verificacao
		Mockito.verify(repository).delete(lancamento);
	}
	
	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoNaoSalvo() {
		//cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();

		//execucao
		Assertions.assertTrue(catchThrowableOfType(() -> service.deletar(lancamento), NullPointerException.class) != null);
		
		//verificacao
		Mockito.verify(repository, Mockito.never()).delete(lancamento);		
	}
	
	@Test
	public void deveFiltrarLancamentos() {
		//cenario
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);	
		
		List<Lancamento> lista = List.of(lancamento);
		Mockito.when(repository.findAll(Mockito.any(Example.class))).thenReturn(lista);
		
		//execucao
		List<Lancamento> resultado = service.buscar(lancamento);
		
		//verificacao
		assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
	}
	
	@Test 
	public void deveAtualizarStatusDeUmLancamento() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
	
		Mockito.doReturn(lancamento).when(service).atualizar(lancamento);
		
		service.atualizarStatus(lancamento, novoStatus);
		
		assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
		Mockito.verify(service).atualizar(lancamento);
	}
	
	@Test
	public void deveObterUmLancamentoPorId() {
		Long id = 1L;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		Assertions.assertTrue(resultado.isPresent());
	}
	
	@Test
	public void deveRetornarVazioQuandoUmLancamentoNaoExiste() {
		Long id = 1L;
		
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		Optional<Lancamento> resultado = service.obterPorId(id);
		
		Assertions.assertFalse(resultado.isPresent());
	}	
	
	@Test
	public void deveLancarErroAoValidarUmLancamento() {
		
		Lancamento lancamento = new Lancamento();
		
		Throwable erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma descrição válida.");
		
		lancamento.setDescricao("descricao");
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um mês válido.");
				
		lancamento.setMes(1);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		lancamento.setAno(2023);
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");		

		lancamento.setUsuario(new Usuario());
		lancamento.getUsuario().setId(1L);

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um valor válido.");
		
		lancamento.setValor(BigDecimal.valueOf(1));
		
		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um tipo de lançamento.");
			
		lancamento.setTipo(TipoLancamento.DESPESA);
	
	} 
}
