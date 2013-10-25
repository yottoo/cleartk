/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */

package org.cleartk.util.ae.linewriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.initializable.InitializableFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.util.CleartkInitializationException;
import org.cleartk.util.ReflectionUtil;
import org.cleartk.util.ViewUriUtil;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * <p>
 * 
 * This writer provides a way to write out annotations one-per-line to a plain text file in a
 * variety of ways configurable at run time.
 * 
 * <p>
 * This class has no relation to LineReader - i.e. LineWriter does not provide
 * "reverse functionality" of LineReader.
 * 
 * <p>
 * If you mistook this class for a line rider, then please redirect to the completely unrelated, but
 * totally awesome Line Rider at: http://linerider.com
 * 
 * @author Philip Ogren
 */

public class LineWriter<ANNOTATION_TYPE extends Annotation, BLOCK_TYPE extends Annotation> extends
    JCasAnnotator_ImplBase {

  private static final String OUTPUT_DIRECTORY_NAME_DESCRIPTION = "takes a "
      + "path to directory into which output files will be written. If no value is "
      + "given for this parameter, then the parameter 'outputFileName' "
      + "is required. If a value is given, then one file for each document/JCas will be created in "
      + "the output directory provided.   The name of each file will be given by the value returned by ViewURIUtil.getURI(jCas). "
      + "If a value for both 'outputDirectoryName' and ' "
      + "outputFileName'  is given, then an exception will be thrown. Example values "
      + "that could be provided might look like: \n\n"
      + "\t/mydata/uima-output/\n"
      + "\tC:/Documents and Settings/User/My Documents/workspace/My Project/data/experiment/output\n";

  public static final String PARAM_OUTPUT_DIRECTORY_NAME = "outputDirectoryName";

  @ConfigurationParameter(
      description = OUTPUT_DIRECTORY_NAME_DESCRIPTION,
      name = PARAM_OUTPUT_DIRECTORY_NAME,
      mandatory = false)
  private String outputDirectoryName;

  private static final String FILE_SUFFIX_DESCRIPTION = "provides a file "
      + "name suffix for each file generated by this writer.  If there is no value "
      + "given for the parameter 'outputDirectoryName', then this parameter is "
      + "ignored. If 'outputDirectoryName' is given a value, then the generated files "
      + "will be named by the document ids and the suffix provided by this  parameter. If no value for this "
      + "parameter is given, then the files will be named the same as the document id. Example values "
      + "that could be provided might include: \n\n" + ".txt\n" + ".tokens\n" + ".annotations.txt";

  public static final String PARAM_FILE_SUFFIX = "fileSuffix";

  @ConfigurationParameter(
      description = FILE_SUFFIX_DESCRIPTION,
      name = PARAM_FILE_SUFFIX,
      mandatory = false)
  private String fileSuffix;

  private static final String OUTPUT_FILE_NAME_DESCRIPTION = "takes a file "
      + "name to write results to.  If no value is given for this parameter, then "
      + "the parameter 'outputDirectoryName' is required.  "
      + "If a value is given, then one file for all documents will be created in the "
      + "output directory provided. If a value for both 'outputDirectoryName'"
      + " and 'outputFileName' is given, then an exception will be thrown. "
      + "Example values that could be provided might look like: \n\n"
      + "/mydata/uima-output/annotations.txt\n"
      + "C:\\Documents and Settings\\User\\My Documents\\workspace\\My Project\\data\\experiment\\output\\output.annotations\n";

  public static final String PARAM_OUTPUT_FILE_NAME = "outputFileName";

  @ConfigurationParameter(
      description = OUTPUT_FILE_NAME_DESCRIPTION,
      name = PARAM_OUTPUT_FILE_NAME,
      mandatory = false)
  private String outputFileName;

  private static final String OUTPUT_ANNOTATION_CLASS_NAME_DESCRIPTION = "takes the name of the annotation class of the annotations that are to be "
      + "written out. The annotation class must be a subclass of org.apache.uima.jcas.tcas.Annotation. "
      + "The manner in which annotations are written out is determined by the AnnotationWriter as "
      + "described below. The AnnotationWriter interface is generically typed. The class specified by this "
      + "parameter must be the same as or a subclass of the type specified by the implementation of "
      + "AnnotationWriter. Example values that could be provided might include:\n\n"
      + "org.apache.uima.jcas.tcas.Annotation (default)\n"
      + "org.cleartk.type.Token\n"
      + "org.cleartk.type.Sentence\n" + "com.yourcompany.yourpackage.YourType";

  public static final String PARAM_OUTPUT_ANNOTATION_CLASS_NAME = "outputAnnotationClassName";

  @ConfigurationParameter(
      name = PARAM_OUTPUT_ANNOTATION_CLASS_NAME,
      mandatory = true,
      description = OUTPUT_ANNOTATION_CLASS_NAME_DESCRIPTION,
      defaultValue = "org.apache.uima.jcas.tcas.Annotation")
  private static String outputAnnotationClassName;

  private static final String ANNOTATION_WRITER_CLASS_NAME_DESCRIPTION = "provides the class name of a class that extends org.cleartk.util.linewriter.AnnotationWriter. "
      + "The AnnotationWriter determines how annotations will be written. For example, "
      + "CoveredTextAnnotationWriter simply writes out the covered text of an annotation. "
      + "Example values that could be provided might include:\n\n"
      + "org.cleartk.util.linewriter.annotation.CoveredTextAnnotationWriter (default)\n"
      + "org.cleartk.util.linewriter.annotation.TokenPOSWriter\n";

  public static final String PARAM_ANNOTATION_WRITER_CLASS_NAME = "annotationWriterClassName";

  @ConfigurationParameter(
      name = PARAM_ANNOTATION_WRITER_CLASS_NAME,
      mandatory = true,
      description = ANNOTATION_WRITER_CLASS_NAME_DESCRIPTION,
      defaultValue = "org.cleartk.util.ae.linewriter.annotation.CoveredTextAnnotationWriter")
  private String annotationWriterClassName;

  private static final String BLOCK_ANNOTATION_CLASS_NAME_DESCRIPTION = "Takes the name of an annotation class that determines a 'block' of lines in the "
      + "resulting output file(s). Each 'block' of lines is separated by some text "
      + "(such as a newline) as determined by the BlockWriter specified as "
      + "described below. If, for example, the value of 'outputAnnotationClassName' is "
      + "'org.cleartk.type.Token' and the value for 'blockAnnotationClassName' is "
      + "'org.cleartk.type.Sentence' and the value for 'blockWriterClassName'  is "
      + "'org.cleartk.util.linewriter.block.BlankLineBlockWriter' (the default), then the tokens in each sentence "
      + "will be written out one per line with a blank line between the last token of a sentence and the first "
      + "token of the following sentence. Note that setting this parameter may limit the number of annotations "
      + "that are written out if, for example, not all tokens are found inside sentences.  If no value is given, then "
      + "there will be no blank lines in the resulting file (assuming the AnnotationWriter does not produce a "
      + "blank line). If you want there to be a blank line between each document (assuming 'outputFileName' "
      + " is given a parameter), then this parameter should be given the value 'org.apache.uima.jcas.tcas.DocumentAnnotation'. "
      + "Example values that could be provided might include: \n\n"
      + "org.cleartk.type.Sentence\n"
      + "org.apache.uima.jcas.tcas.DocumentAnnotation\n" + "com.yourcompany.yourpackage.YourType\n";

  public static final String PARAM_BLOCK_ANNOTATION_CLASS_NAME = "blockAnnotationClassName";

  @ConfigurationParameter(
      description = BLOCK_ANNOTATION_CLASS_NAME_DESCRIPTION,
      name = PARAM_BLOCK_ANNOTATION_CLASS_NAME,
      mandatory = false)
  private String blockAnnotationClassName;

  private final static String BLOCK_WRITER_CLASS_NAME_DESCRIPTION = "Provides  the class name of a class that extends org.cleartk.util.linewriter.BlockWriter. "
      + "The BlockWriter determines how blocks of annotations will be delimited. For example, "
      + "org.cleartk.util.linewriter.block.BlankLineBlockWriter simply writes out a blank line between each "
      + "block of annotations.  Example values that could be provided might include: \n\n"
      + "org.cleartk.util.linewriter.block.BlankLineBlockWriter\n"
      + "org.cleartk.util.linewriter.block.DocumentIdBlockWriter\n";

  public static final String PARAM_BLOCK_WRITER_CLASS_NAME = "blockWriterClassName";

  @ConfigurationParameter(
      name = PARAM_BLOCK_WRITER_CLASS_NAME,
      description = BLOCK_WRITER_CLASS_NAME_DESCRIPTION,
      mandatory = false,
      defaultValue = "org.cleartk.util.ae.linewriter.block.BlankLineBlockWriter")
  private String blockWriterClassName;

  private File outputDirectory;

  private File outputFile;

  private Class<? extends Annotation> outputAnnotationClass;

  private Type outputAnnotationType;

  private Class<? extends Annotation> blockAnnotationClass;

  private Type blockAnnotationType;

  boolean blockOnDocument = false;

  AnnotationWriter<ANNOTATION_TYPE> annotationWriter;

  BlockWriter<BLOCK_TYPE> blockWriter;

  PrintStream out;

  private boolean typesInitialized = false;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    try {
      super.initialize(context);

      if ((outputDirectoryName == null && outputFileName == null)
          || (outputDirectoryName != null && outputFileName != null)) {
        throw CleartkInitializationException.notExactlyOneParameterSet(
            PARAM_OUTPUT_DIRECTORY_NAME,
            outputDirectoryName,
            PARAM_OUTPUT_FILE_NAME,
            outputFileName);
      }

      if (outputDirectoryName != null) {
        outputDirectory = new File(outputDirectoryName);
        if (!this.outputDirectory.exists()) {
          this.outputDirectory.mkdirs();
        }
      }

      if (outputFileName != null) {
        outputFile = new File(outputFileName);
        if (!outputFile.getParentFile().exists()) {
          outputFile.getParentFile().mkdirs();
        }
        out = new PrintStream(outputFile);
      }

      outputAnnotationClass = InitializableFactory.getClass(
          outputAnnotationClassName,
          Annotation.class);

      Class<? extends AnnotationWriter<ANNOTATION_TYPE>> annotationWriterClass = ReflectionUtil.uncheckedCast(Class.forName(
          annotationWriterClassName).asSubclass(AnnotationWriter.class));
      annotationWriter = InitializableFactory.create(
          context,
          annotationWriterClassName,
          annotationWriterClass);

      java.lang.reflect.Type annotationType = ReflectionUtil.getTypeArgument(
          AnnotationWriter.class,
          "ANNOTATION_TYPE",
          this.annotationWriter);

      if (!ReflectionUtil.isAssignableFrom(annotationType, outputAnnotationClass)) {
        throw CleartkInitializationException.incompatibleTypeParameterAndType(
            this.annotationWriter,
            "ANNOTATION_TYPE",
            annotationType,
            outputAnnotationClass);
      }

      if (blockAnnotationClassName != null) {

        Class<? extends BlockWriter<BLOCK_TYPE>> blockWriterClass = ReflectionUtil.uncheckedCast(Class.forName(
            blockWriterClassName).asSubclass(BlockWriter.class));
        this.blockWriter = InitializableFactory.create(
            context,
            blockWriterClassName,
            blockWriterClass);

        if (blockAnnotationClassName.equals("org.apache.uima.jcas.tcas.DocumentAnnotation")) {
          blockOnDocument = true;
        } else {
          blockAnnotationClass = Class.forName(blockAnnotationClassName).asSubclass(
              Annotation.class);

          java.lang.reflect.Type blockType = ReflectionUtil.getTypeArgument(
              BlockWriter.class,
              "BLOCK_TYPE",
              this.blockWriter);

          if (!ReflectionUtil.isAssignableFrom(blockType, blockAnnotationClass)) {
            throw CleartkInitializationException.incompatibleTypeParameterAndType(
                this.blockWriter,
                "BLOCK_TYPE",
                blockType,
                blockAnnotationClass);
          }
        }
      }

      if (fileSuffix == null) {
        fileSuffix = "";
      } else if (!fileSuffix.startsWith(".")) {
        fileSuffix = "." + fileSuffix;
      }
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

  }

  private void initializeTypes(JCas jCas) throws AnalysisEngineProcessException {
    try {
      outputAnnotationType = JCasUtil.getType(jCas, outputAnnotationClass);
      if (blockAnnotationClass != null) {
        blockAnnotationType = JCasUtil.getType(jCas, blockAnnotationClass);
      }
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
    typesInitialized = true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    if (!typesInitialized)
      initializeTypes(jCas);

    try {
      if (outputDirectory != null) {
        String id = (new File(ViewUriUtil.getURI(jCas))).getName();
        while (id.endsWith(".")) {
          id = id.substring(0, id.length() - 1);
        }
        out = new PrintStream(new File(outputDirectory, id + fileSuffix));
      }

      if (blockOnDocument) {
        BLOCK_TYPE documentAnnotation = (BLOCK_TYPE) jCas.getDocumentAnnotationFs();
        out.print(blockWriter.writeBlock(jCas, documentAnnotation));
        FSIterator<Annotation> outputAnnotations = jCas.getAnnotationIndex(outputAnnotationType).iterator();
        while (outputAnnotations.hasNext()) {
          ANNOTATION_TYPE outputAnnotation = (ANNOTATION_TYPE) outputAnnotations.next();
          out.println(annotationWriter.writeAnnotation(jCas, outputAnnotation));
        }
      } else if (blockAnnotationType != null) {
        FSIterator<Annotation> blocks = jCas.getAnnotationIndex(blockAnnotationType).iterator();
        while (blocks.hasNext()) {
          BLOCK_TYPE blockAnnotation = (BLOCK_TYPE) blocks.next();
          out.print(blockWriter.writeBlock(jCas, blockAnnotation));
          FSIterator<Annotation> outputAnnotations = jCas.getAnnotationIndex(outputAnnotationType).subiterator(
              blockAnnotation);
          while (outputAnnotations.hasNext()) {
            ANNOTATION_TYPE outputAnnotation = (ANNOTATION_TYPE) outputAnnotations.next();
            out.println(annotationWriter.writeAnnotation(jCas, outputAnnotation));
          }
        }
      }

      else {
        FSIterator<Annotation> outputAnnotations = jCas.getAnnotationIndex(outputAnnotationType).iterator();
        while (outputAnnotations.hasNext()) {
          ANNOTATION_TYPE outputAnnotation = (ANNOTATION_TYPE) outputAnnotations.next();
          out.println(annotationWriter.writeAnnotation(jCas, outputAnnotation));
        }
      }

      if (outputDirectory != null) {
        out.flush();
        out.close();
      }
    } catch (FileNotFoundException fnfe) {
      throw new AnalysisEngineProcessException(fnfe);
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    if (outputFile != null) {
      out.flush();
      out.close();
    }
    // TODO Auto-generated method stub
    super.collectionProcessComplete();
  }

}
