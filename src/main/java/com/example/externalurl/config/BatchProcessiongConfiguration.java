
@Configuration
public class BatchProcessingConfiguration {
    @Bean
    public RestTemplate restTemplate() { return new RestTemplate(); }

    @Bean
    public ObjectMapper objectMapper() { return new ObjectMapper(); }

    @Bean
    public DocumentBuilderFactory documentBuilderFactory() { return DocumentBuilderFactory.newInstance(); }
    
}