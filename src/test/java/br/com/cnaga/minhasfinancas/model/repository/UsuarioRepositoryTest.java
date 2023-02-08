package br.com.cnaga.minhasfinancas.model.repository;

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

import br.com.cnaga.minhasfinancas.model.entity.Usuario;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioRepositoryTest {

	@Autowired
	UsuarioRepository repository;
	
	@Autowired
	TestEntityManager entityManager;
	
	@Test
	public void deveVerificarAExistenciaDeUmEmail() {
	
		//cenario
		Usuario usuario = criarUsuario();
		entityManager.persist(usuario);
		
		//acao/execucao
		boolean result = repository.existsByEmail("email@email.com");
		
		//verificacao
		Assertions.assertTrue(result);
		
	} 
	
	@Test
	public void deveRetornarFalsoQuandoNaoHouverUsuarioCadstradoComEmail() {
		
		//cenario
		
		//acao
		boolean result = repository.existsByEmail("email@email.com");
		
		//verificacao
		Assertions.assertFalse(result);
	}
	
	@Test
	public void devePersistirUsuarioNaBaseDeDados() {
		//cenario
		Usuario usuario = criarUsuario();
		
		//acao
		Usuario usuarioSalvo = repository.save(usuario);
		
		//verificacao
		Assertions.assertNotNull(usuarioSalvo.getId());
	}
	
	@Test
	public void deveBuscarUmUsuarioPorEmail() {
		//cenario
		Usuario usuario = criarUsuario();
		entityManager.persist(usuario);
		
		//acao
		Optional<Usuario> result = repository.findByEmail("email@email.com");
		
		//verificacao
		Assertions.assertTrue(result.isPresent());
		
	}
	
	@Test
	public void deveRetornarVazioAoBuscarUmUsuarioPorEmailQuandoNaoEncontrado() {
		//cenario
		
		//acao
		Optional<Usuario> result = repository.findByEmail("email@email.com");
		
		//verificacao
		Assertions.assertFalse(result.isPresent());
		
	}	
	
	public static Usuario criarUsuario() {
		return  Usuario
				.builder()
				.nome("usuario")
				.email("email@email.com")
				.senha("123")
				.build();
	}
}
