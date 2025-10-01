package com.fighthub.model;

import com.fighthub.dto.usuario.UsuarioUpdateCompletoRequest;
import com.fighthub.dto.usuario.UsuarioUpdateParcialRequest;
import com.fighthub.mapper.EnderecoMapper;
import com.fighthub.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @Column(nullable = false, length = 100)
    private String nome;

    @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Setter
    @Column(length = 255)
    private String senha;

    @Setter
    @Column(length = 255)
    private String foto;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    @Setter
    @Column(name = "login_social", nullable = false)
    private boolean loginSocial;

    @Setter
    @Column(nullable = false)
    private boolean ativo;

    @Setter
    @Column(length = 15)
    private String telefone;

    @Setter
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Setter
    @Embedded
    private Endereco endereco;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo;
    }

    public void putUpdate(UsuarioUpdateCompletoRequest request) {
        this.setNome(request.nome());
        this.setEmail(request.email());
        this.setFoto(request.foto());
        this.setTelefone(request.telefone());
        this.setCpf(request.cpf());
        this.setEndereco(EnderecoMapper.toEntity(request.endereco()));
        this.setRole(request.role());
        this.setAtivo(request.ativo());
    }

    public void patchUpdate(UsuarioUpdateParcialRequest request) {
        if (request.nome() != null) { this.setNome(request.nome()); }
        if (request.email() != null) { this.setEmail(request.email()); }
        if (request.foto() != null) { this.setFoto(request.foto()); }
        if (request.telefone() != null) { this.setTelefone(request.telefone()); }
        if (request.cpf() != null) { this.setCpf(request.cpf()); }
        if (request.endereco() != null) { this.setEndereco(EnderecoMapper.toEntity(request.endereco())); }
        if (request.role() != null) { this.setRole(request.role()); }
        if (request.ativo() != null) { this.setAtivo(request.ativo()); }
    }
}