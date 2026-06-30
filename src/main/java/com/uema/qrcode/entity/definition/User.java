package com.uema.qrcode.entity.definition;
import com.uema.qrcode.entity.definition.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;
    private String username;
    private String password;
    @Column(unique = true, nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "registry",
            joinColumns = @JoinColumn(name = "user_id")
    )
    private Set<Registry> registry;


    public void deleteRegistry(Registry registry){
        this.registry.remove(registry);
    }

    public void addRegistry(Registry registry){
        this.registry.add(registry);
    }
    public void setProject(Registry registry, Consumer<Registry> consumer){
        consumer.accept(registry);
        this.registry.add(registry);
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

}