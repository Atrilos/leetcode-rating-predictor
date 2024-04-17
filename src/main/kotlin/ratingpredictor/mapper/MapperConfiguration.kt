package ratingpredictor.mapper

import org.modelmapper.ModelMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MapperConfiguration {

    @Bean
    fun modelMapper(): ModelMapper {
        val mapper = ModelMapper()
        addAllMappings(mapper)
        mapper.configuration.setSkipNullEnabled(true)

        return mapper
    }

    private fun addAllMappings(mapper: ModelMapper) {
        TODO()
    }
}