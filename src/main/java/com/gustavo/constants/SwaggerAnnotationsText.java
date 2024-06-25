package com.gustavo.constants;

public interface SwaggerAnnotationsText {
    String SIMPLE_ANNOTATION_TEXT = """
            @Operation(summary = "summary", description = "description"
            """;
    String GET_ANNOTATION_TEXT = """
            @Operation(summary = "get summary", description = "Get description",
                        method = "GET",
                        responses = @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(schema = @Schema(implementation = ResponseDto.class))}))
            """;

    String GET_LIST_ANNOTATION_TEXT = """
            @Operation(summary = "list of your dto with filters", description = "List something with filter in query",
                        method = "GET",
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
                                     method = "POST",
                                     requestBody = @RequestBody(content = {@Content(schema = @Schema(implementation = PostDto.class))}),
                                     responses = @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(schema = @Schema(implementation = ResponseDto.class))})
                             )
                    """;

    String PUT_ANNOTATION_TEXT = """
            @Operation(summary = "update", description = "update description",
                               method = "PUT",
                                      requestBody = @RequestBody(content = {@Content(schema = @Schema(implementation = Dto.class))}),
                                      responses = @ApiResponse(responseCode = "200", description = "Ok", content = {@Content(schema = @Schema(implementation = ResponseDto.class))})
                              )
            """;

    String DELETE_ANNOTATION_TEXT =
            """
                    @Operation(summary = "Delete operation", description = "Request to delete something", method = "DELETE", responses = @ApiResponse(responseCode = "200", description = "Ok"))
                    """;

}
