package io.joern.rustsrc2cpg.parser

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.gson.Gson
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializer
import io.joern.rustsrc2cpg.ast.*
import io.shiftleft.utils.IOUtils

import java.nio.file.Paths
import scala.collection.mutable.ListBuffer

class JsonParser {
  private val gson                     = new Gson()
  private val objectMapper             = new ObjectMapper()
  private val customDeserializerModule = new SimpleModule()

  objectMapper
    .registerModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
    .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
    .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
    .configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true)
    .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true)
    .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, true)
    .configure(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, true)
  // .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)

  // Register custom deserializer
  customDeserializerModule.addDeserializer(classOf[Type], new TypeDeserializer)
  customDeserializerModule.addDeserializer(classOf[Fields], new FieldsDeserializer)
  customDeserializerModule.addDeserializer(classOf[PathArguments], new PathArgumentsDeserializer)
  customDeserializerModule.addDeserializer(classOf[PathArguments], new PathArgumentsDeserializer)
  customDeserializerModule.addDeserializer(classOf[UseTree], new UseTreeDeserializer)
  customDeserializerModule.addDeserializer(classOf[Visibility], new VisibilityDeserializer)
  objectMapper.registerModule(customDeserializerModule)

  def parse(filepath: String): FileAst = {
    val fileContent = IOUtils.readEntireFile(Paths.get(filepath))
    val fileAst     = objectMapper.readValue(fileContent, classOf[FileAst])
    fileAst
  }
}
