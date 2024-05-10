package com.gustavo.constants;

public interface SwaggerAnnotationsText {
     String GET_ANNOTATION_TEXT = """
            @Operation(summary = "get summary", description = "Get description",
                        requestBody = @RequestBody(content = {@Content(schema = @Schema(implementation = DTO.class))}),
                        responses = @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}))
            """;

     String GET_LIST_ANNOTATION_TEXT = """
@Operation(summary = "list of your dto with filters", description = "List something with filter in query",
            parameters = {
                    @Parameter(name = "orderBy", in = ParameterIn.QUERY, description = "Order by orderBy", schema = @Schema(implementation = String.class)),
            },
            responses = @ApiResponse(responseCode = "200", description = "Ok", content = {
                    @Content(schema = @Schema(implementation = YourDto.class))
            })
    )""";

     String POST_ANNOTATION_TEXT =
            """
           @Operation(summary = "Your summary", description = "Your description",
                            requestBody = @RequestBody(content = {@Content(schema = @Schema(implementation = PostDto.class))}),
                            responses = @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(schema = @Schema(implementation = ResponseDto.class))})
                    )
           """;

     String PUT_ANNOTATION_TEXT = """
           @Operation(summary = "update", description = "update description",
                                     requestBody = @RequestBody(content = {@Content(schema = @Schema(implementation = Dto.class))}),
                                     responses = @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(schema = @Schema(implementation = ResponseDto.class))})
                             )
           """;

     String DELETE_ANNOTATION_TEXT =
            """
            @Operation(summary = "Delete operation", description = "Request to delete something", responses = @ApiResponse(responseCode = "200", description = "Ok"))
            """;

}
