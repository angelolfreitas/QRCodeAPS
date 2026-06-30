package com.uema.qrcode.entity.definition;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@EqualsAndHashCode(of = "linkAwsS3")
public class Registry {
    @Column(name = "author_user_id")
    private String userId; // O ID do usuário que a provocou
    private String linkAwsS3;
    private LocalDateTime dataRegistro = LocalDateTime.now();
}
