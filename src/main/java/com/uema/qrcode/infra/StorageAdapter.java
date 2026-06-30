package com.uema.qrcode.infra;

import com.uema.qrcode.infra.port.StoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
/// Implmementação de StoragePort, que será o ponto de entrada do endereço TCP-IP
/// das interações do usuário com esta API.
/// Não dependemos desse storage, pois estaremos lidando com uma storageport em todas as classes
/// que demandarem desse serviço. Isso é essencial para a escalabilidade do projeto.
///
/// @Component adapta essa implementação ao ecossistema do spingboot. Ele agora reconhece que isso é um
/// lugar onde as requisições serão armazenadas.
/// Também indica qual implementação de StorgePort usar.
@Component
public class StorageAdapter implements StoragePort {
    //classe que interage meu sistema com o AWS
    private final S3Client s3Client;
    //identificador do lugar em que os dados serão armazenados
    private final String bucketName;
    //região geográfica do dado
    private final String region;
    //técnica para recuperar constantes
    public StorageAdapter(@Value("${aws.s3.region}") String region,
                          @Value("${aws.s3.bucket-name}") String bucketName) {

        this.bucketName = bucketName;
        this.region = region;
        // Esse linha vai converter nossa string de região em uma enumeração
        // específica do aws.  Semelhante ao que eu fiz com GraphType no projeto
        // da p1 de EDA.
        this.s3Client = S3Client.builder()
                .region(Region.of(this.region)).build();
    }

    @Override
    public String uploadFile(byte[] file, String fileName, String contentType) {
        //request da aws. É o processo reverso do que fizemos no contorller:
        //esse objeto vai ser seriallizado para o aws e gerará uma
        //resposta acerca da inserção do arquivo.
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                //caminho para colocar o objeto
                .bucket(bucketName)
                //chave de acesso dentro do banco do aws
                .key(fileName)
                //tipo do arquivo. A gente pode salvar qualquer coisa.
                .contentType(contentType)
                .build();
        //regra de arthemis
        //monta o corpo a partir dos dados serializados do arquivo.
        RequestBody fileRequest = RequestBody.fromBytes(file);
        s3Client.putObject(putObjectRequest, fileRequest);
        // vemos que o bucket é a maior hierrarquia na url da localização dos arquivos upados
        // depois, acessamos a região geográfica do servidor desse arquivo.
        // por fim, acessamos o requerimento feito pelo request do primeiro parâmetro do cliente.
        String fileLocation = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        return fileLocation;
    }
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // Extrai o nome do arquivo (Key) a partir do final da URL pública do S3
            // Exemplo: https://bucket.s3.amazonaws.com/meu-qrcode-uuid -> meu-qrcode-uuid
            String fileKey = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            System.out.println("Arquivo deletado com sucesso do S3: " + fileKey);
        } catch (Exception e) {
            // Lança uma exceção ou faz um log caso a política do IAM barre a exclusão
            throw new RuntimeException("Falha ao deletar arquivo físico no S3: " + e.getMessage());
        }
    }
}