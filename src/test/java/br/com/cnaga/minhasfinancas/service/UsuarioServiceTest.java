package br.com.cnaga.minhasfinancas.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.cnaga.minhasfinancas.exception.ErroAutenticacao;
import br.com.cnaga.minhasfinancas.exception.RegraNegocioException;
import br.com.cnaga.minhasfinancas.model.entity.Usuario;
import br.com.cnaga.minhasfinancas.model.repository.UsuarioRepository;
import br.com.cnaga.minhasfinancas.service.impl.UsuarioServiceImpl;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

	@SpyBean
	UsuarioServiceImpl service;

	@MockBean
	UsuarioRepository repository;

	String email = "email@email.com";
	String senha = "senha";

	@Test
	public void deveSalvarUmUsuario() {
		Assertions.assertDoesNotThrow(() -> {
			// cenario
			Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
			Usuario usuario = Usuario.builder().nome("Nome").email(email).senha(senha).id(1L).build();
			Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

			// acao
			Usuario usuarioSalvo = service.salvarUsuario(new Usuario());

			// verificacao
			Assertions.assertNotNull(usuarioSalvo);
			Assertions.assertEquals(usuarioSalvo.getId(), 1L);
			Assertions.assertEquals(usuarioSalvo.getNome(),"Nome");
			Assertions.assertEquals(usuarioSalvo.getEmail(), email);
			Assertions.assertEquals(usuarioSalvo.getSenha(), senha);				

		});
	}

	@Test
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
		Assertions.assertThrows(RegraNegocioException.class, () -> {
			// cenario
			Usuario usuario = Usuario.builder().nome("Nome").email(email).senha(senha).id(1L).build();
			Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

			// acao
			service.salvarUsuario(usuario);

			// verificacao
			Mockito.verify(repository, Mockito.never()).save(usuario);
		});
	}
	
	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
		Assertions.assertDoesNotThrow(() -> {
			// cenario
			Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
			Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));

			// acao
			Usuario result = service.autenticar(email, senha);

			// verificacao
			Assertions.assertNotNull(result);
		});
	}

	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
		Assertions.assertThrows(ErroAutenticacao.class, () -> {
			// cenario
			Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

			// acao
			service.autenticar(email, senha);
		});
	}

	@Test
	public void deveLancarErroQuandoSenhaForInvalida() {
		Assertions.assertThrows(ErroAutenticacao.class, () -> {
			// cenario
			Usuario usuario = Usuario.builder().email(email).senha(senha).id(1L).build();
			Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));

			// acao
			service.autenticar(email, "123");

		});
	}

	@Test
	public void deveValidarEmail() {
		Assertions.assertDoesNotThrow(() -> {
			// cenario
			Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);

			// acao
			service.validarEmail(email);
		});
	}

	@Test
	public void deveLancarErroAoValidarQuandoExistirEmailCadastrado() {
		Assertions.assertThrows(RegraNegocioException.class, () -> {
			// cenario
			Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);

			// acao
			service.validarEmail(email);
		});
	}

}
