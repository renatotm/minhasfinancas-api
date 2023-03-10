package br.com.cnaga.minhasfinancas.api.resource;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.cnaga.minhasfinancas.api.dto.AtualizaStatusDTO;
import br.com.cnaga.minhasfinancas.api.dto.LancamentoDTO;
import br.com.cnaga.minhasfinancas.exception.RegraNegocioException;
import br.com.cnaga.minhasfinancas.model.entity.Lancamento;
import br.com.cnaga.minhasfinancas.model.entity.Usuario;
import br.com.cnaga.minhasfinancas.model.enums.StatusLancamento;
import br.com.cnaga.minhasfinancas.model.enums.TipoLancamento;
import br.com.cnaga.minhasfinancas.service.LancamentoService;
import br.com.cnaga.minhasfinancas.service.UsuarioService;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoResource {

	@Autowired
	private LancamentoService service;
	@Autowired
	private UsuarioService usuarioService;
	
	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO dto) {
		try {
			Lancamento entidade = converter(dto);
			entidade = service.salvar(entidade);
			URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(entidade.getId())
					.toUri();
			return ResponseEntity.created(uri).body(entidade);
		} catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PutMapping("{id}")
	public ResponseEntity atualizar(@PathVariable Long id, @RequestBody LancamentoDTO dto) {
			return service.obterPorId(id).map(entity -> {
				try {
					Lancamento lancamento = converter(dto);
					lancamento.setId(entity.getId());
					service.atualizar(lancamento);
					return ResponseEntity.ok(lancamento);						
				} catch (RegraNegocioException e) {
					return ResponseEntity.badRequest().body(e.getMessage());
				}
			}).orElseGet(() -> new ResponseEntity("Lan??amento n??o encontrado.", HttpStatus.BAD_REQUEST));
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity deletar(@PathVariable("id") Long id) {
		return service.obterPorId(id).map(entity -> {
				service.deletar(entity);
				return new ResponseEntity(HttpStatus.NO_CONTENT);						
		}).orElseGet(() -> new ResponseEntity("Lan??amento n??o encontrado.", HttpStatus.BAD_REQUEST));		
	}
	
	@GetMapping
	public ResponseEntity buscar(
		@RequestParam(value = "descricao", required = false) String descricao,
		@RequestParam(value = "mes", required = false) Integer mes,
		@RequestParam(value = "ano", required = false) Integer ano,
		@RequestParam(value = "valor", required = false) BigDecimal valor,
		@RequestParam(value = "usuario", required = true) Long idUsuario,
		@RequestParam(value = "tipo", required = false) TipoLancamento tipo,
		@RequestParam(value = "status", required = false) StatusLancamento status		
		) {
		
		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
		lancamentoFiltro.setValor(valor);
		
		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
		if(!usuario.isPresent()) {
			return ResponseEntity.badRequest().body("Usu??rio n??o encontrado para o id informado.");
		} else {
			lancamentoFiltro.setUsuario(usuario.get());
		}

		lancamentoFiltro.setTipo(tipo);
		lancamentoFiltro.setStatus(status);
		
		List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
		
		return ResponseEntity.ok(lancamentos);
	}
	
	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus(@PathVariable("id") Long id,  @RequestBody AtualizaStatusDTO dto) {
		return service.obterPorId(id).map( entity -> {
			StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());
			if(statusSelecionado == null) {
				return ResponseEntity.badRequest().body("Status informado inv??lido.");
			}
			try {
				entity.setStatus(statusSelecionado);
				service.atualizar(entity);
				return ResponseEntity.ok(entity);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
			
		}).orElseGet(() -> new ResponseEntity("Lan??amento n??o encontrado.", HttpStatus.BAD_REQUEST));
	}
	
	private Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		
		Usuario usuario = usuarioService.obterPorId(dto.getUsuario())
				.orElseThrow(() -> new RegraNegocioException("Usu??rio n??o encontrado com o id informado."));
		lancamento.setUsuario(usuario);
		if(dto.getTipo() != null) {
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
		}	
		if(dto.getStatus() != null) {
			lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
		}	
		return lancamento;
	}
}
