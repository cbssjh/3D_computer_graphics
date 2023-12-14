/*
 * I declare that this code os my own work
 * Author Junhyeok Seo, jseo10@sheffield.ac.uk
 */
import gmaths.*;

import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;

public class Aliens_GLEventListener implements GLEventListener {
  private boolean updateBranchesEnabled = true;
  private boolean updateBranchesEnabled2 = true;
  private boolean isLightOn = true;

  private static final boolean DISPLAY_SHADERS = false;

  public Aliens_GLEventListener(Camera camera) {
    this.camera = camera;
    this.camera.setPosition(new Vec3(4f, 12f, 18f));
  }

  // ***************************************************
  /*
   * METHODS DEFINED BY GLEventListener
   */

  /* Initialisation */
  public void init(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
    gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LESS);
    gl.glFrontFace(GL.GL_CCW); // default is 'CCW'
    gl.glEnable(GL.GL_CULL_FACE); // default is 'not enabled'
    gl.glCullFace(GL.GL_BACK); // default is 'back', assuming CCW
    initialise(gl);
    startTime = getSeconds();
  }

  /* Called to indicate the drawing surface has been moved and/or resized */
  public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
    GL3 gl = drawable.getGL().getGL3();
    gl.glViewport(x, y, width, height);
    float aspect = (float) width / (float) height;
    camera.setPerspectiveMatrix(Mat4Transform.perspective(45, aspect));
  }

  /* Draw */
  public void display(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    render(gl);
  }

  /* Clean up memory, if necessary */
  public void dispose(GLAutoDrawable drawable) {
    GL3 gl = drawable.getGL().getGL3();
    light.dispose(gl);
    floor.dispose(gl);
    sphere.dispose(gl);
    pole.dispose(gl);
  }

  // ***************************************************
  /*
   * INTERACTION
   *
   *
   */

  public void incXPosition() {
    xPosition += 0.5f;
    if (xPosition > 5f)
      xPosition = 5f;
    updateX();
  }

  public void decXPosition() {
    xPosition -= 0.5f;
    if (xPosition < -5f)
      xPosition = 5f;
    updateX();
  }

  private void updateX() {
    translateX.setTransform(Mat4Transform.translate(xPosition, 0, 0));
    translateX.update(); // IMPORTANT the scene graph has changed
  }

  // ***************************************************
  /*
   * THE SCENE
   * Now define all the methods to handle the scene.
   * This will be added to in later examples.
   */

  private Camera camera;
  private Mat4 perspective;
  private Model floor, pole, sphere, wallModel;
  private Light light, gen_light, gen_light2;
  private SGNode twoBranchRoot, wallBranchRoot, twoBranchRoot2;

  private TransformNode translateX, rotateAll, rotateUpper, rotateUpperArm, rotateUpperArm2, rotateWallUpper;
  private TransformNode translateX2, rotateAll2, rotateUpper2, rotateUpperArm_2, rotateUpperArm2_2;
  private float xPosition = 0;
  private float xPosition2 = 5;
  private float rotateAllAngleStart = 25, rotateAllAngle = rotateAllAngleStart;
  private float rotateUpperAngleStart = -60, rotateUpperAngle = rotateUpperAngleStart;
  private float rotateUpperAngleStartArm = 60, rotateUpperAngleArm = rotateUpperAngleStartArm; 
  private float rotateUpperAngleStartArm2 = -60, rotateUpperAngleArm2 = rotateUpperAngleStartArm2; 
  private float rotateWall = 90, rotateWallAngle = rotateWall;

  private void initialise(GL3 gl) {
    createRandomNumbers();
    int[] textureId3 = TextureLibrary.loadTexture(gl, "textures/snow.jpg");
    int[] textureId4 = TextureLibrary.loadTexture(gl, "textures/silver_master.jpg");
    int[] textureId5 = TextureLibrary.loadTexture(gl, "textures/metal.jpg");
    int[] textureId6 = TextureLibrary.loadTexture(gl, "textures/metal_specular.jpg");
    int[] textureId7 = TextureLibrary.loadTexture(gl, "textures/silver_master_specular.jpg");
    int[] textureId8 = TextureLibrary.loadTexture(gl, "textures/trees.jpg");
    int[] textureId9 = TextureLibrary.loadTexture(gl, "textures/silver2_master.jpg");
    int[] textureId10 = TextureLibrary.loadTexture(gl, "textures/silver2_master_specular.jpg");
    
    // Security Light
    light = new Light(gl);
    light.setCamera(camera);
    
    // General Lights
    gen_light = new Light(gl);
    gen_light.setCamera(camera);

    gen_light2 = new Light(gl);
    gen_light2.setCamera(camera);
    

    // Floor
    Mesh mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    Shader shader = new Shader(gl, "vs_tt_05.txt", "fs_sphere_04.txt"); // using fs_sphere, because it always looking
                                                                        // blue
    Material material = new Material(new Vec3(0.0f, 0.5f, 0.81f), new Vec3(0.0f, 0.5f, 0.81f),
        new Vec3(0.3f, 0.3f, 0.3f), 32.0f);
    Mat4 modelMatrix = Mat4Transform.scale(20, 1f, 20);
    floor = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId3);

    // Security Pole
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_sphere_04.txt", "fs_sphere_04.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(0.5f,13,0.5f), Mat4Transform.translate(-12,0.5f,0));
    pole = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId5, textureId6);

    // Wall
    mesh = new Mesh(gl, TwoTriangles.vertices.clone(), TwoTriangles.indices.clone());
    shader = new Shader(gl, "vs_tt_05.txt", "fs_sphere_04.txt");
    material = new Material(new Vec3(10f, 10f, 10f), new Vec3(0.5f, 0.5f, 0.5f), new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.translate(0, 4, 0), Mat4Transform.scale(5, 1f, 5));
    modelMatrix = Mat4.multiply(Mat4Transform.translate(-3, 4, -3), modelMatrix);
    wallModel = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId8);

    wallBranchRoot = new NameNode("wall-branch structure");
    NameNode wallLowerBranch = new NameNode("wall lower branch");

    Mat4 m2 = Mat4Transform.scale(0, 0, 0);
    m2 = Mat4.multiply(m2, Mat4Transform.translate(0, 0.5f, 0));
    TransformNode wallTransformNode = new TransformNode("scale(2,4,2); translate(0,0.5,0)", m2);
    ModelNode wallLowerBranchShape = new ModelNode("wall(0)", wallModel);
    TransformNode walltranslateToTop = new TransformNode("translate(0,4,0)", Mat4Transform.translate(3, 4, 0));

    Mat4 testTransform = Mat4Transform.scale(20f, 1f, 20f);
    testTransform = Mat4.multiply(testTransform, Mat4Transform.translate(0.0f, -10.0f, -0.5f));
    TransformNode testTransformNode = new TransformNode("scale(0.2f,1.0f,0.2f);translate(0,0.5,0)", testTransform);
    ModelNode testModelNode = new ModelNode("wall(2)", wallModel);

    rotateWallUpper = new TransformNode("rotateAroundZ(" + rotateWallAngle + ")",
        Mat4Transform.rotateAroundX(rotateWallAngle));

    wallBranchRoot.addChild(wallLowerBranch);

      wallLowerBranch.addChild(wallTransformNode);
      wallTransformNode.addChild(wallLowerBranchShape);

      wallLowerBranch.addChild(rotateWallUpper);
      rotateWallUpper.addChild(testTransformNode);
      testTransformNode.addChild(testModelNode);

    wallBranchRoot.update();

    // Original Alien
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_sphere_04.txt", "fs_sphere_04.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f),
        32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(4, 4, 4), Mat4Transform.translate(0, 0.5f, 0));
    sphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId4, textureId7);

    twoBranchRoot = new NameNode("two-branch structure");
    translateX = new TransformNode("translate(" + xPosition + ",0,0)", Mat4Transform.translate(xPosition, 0, 0));
    rotateAll = new TransformNode("rotateAroundZ(" + rotateAllAngle + ")", Mat4Transform.rotateAroundZ(rotateAllAngle));
    NameNode lowerBranch = new NameNode("lower branch");
    Mat4 m = Mat4Transform.scale(3, 3, 3);
    m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
    TransformNode makeLowerBranch = new TransformNode("scale(2.5,4,2.5); translate(0,0.5,0)", m);
    ModelNode cube0Node = new ModelNode("Sphere(0)", sphere);
    TransformNode translateToTop = new TransformNode("translate(0,4,0)", Mat4Transform.translate(0, 4, 0));
    rotateUpper = new TransformNode("rotateAroundZ(" + rotateUpperAngle + ")",Mat4Transform.rotateAroundZ(rotateUpperAngle));
    rotateUpperArm = new TransformNode("rotateAroundZ(" + rotateUpperAngleArm + ")",Mat4Transform.rotateAroundZ(rotateUpperAngleArm));
    rotateUpperArm2 = new TransformNode("rotateAroundZ(" + rotateUpperAngleArm2 + ")",Mat4Transform.rotateAroundZ(rotateUpperAngleArm2));
    NameNode upperBranch = new NameNode("upper branch");
    m = Mat4Transform.scale(2f, 2f, 2f);
    m = Mat4.multiply(m, Mat4Transform.translate(0, 0, 0));
    TransformNode makeUpperBranch = new TransformNode("scale(1.4f,3.1f,1.4f);translate(0,0.5,0)", m);
    ModelNode cube1Node = new ModelNode("Sphere(1)", sphere);

    // Horn
    Mat4 newSphereTransform = Mat4Transform.scale(0.1f, 1.0f, 0.1f);
    newSphereTransform = Mat4.multiply(newSphereTransform, Mat4Transform.translate(0, 0.5f, 0));
    TransformNode newSphereTransformNode = new TransformNode("scale(1.4f,3.9f,1.4f);translate(0,0.5,0)", newSphereTransform);
    ModelNode newSphereModelNode = new ModelNode("Sphere(2)", sphere);

    // Top of horn
    Mat4 thirdSphereTransform = Mat4Transform.scale(0.3f, 0.3f, 0.3f);
    thirdSphereTransform = Mat4.multiply(thirdSphereTransform, Mat4Transform.translate(0, 7.1f, 0));
    TransformNode thirdSphereTransformNode = new TransformNode("scale(0.25f,2.0f,0.25f);translate(0,0.5,0)", thirdSphereTransform);
    ModelNode thirdSphereModelNode = new ModelNode("Sphere(3)", sphere);

    // Ears
    Mat4 fourthSphereTransform = Mat4Transform.scale(0.3f, 1.5f, 0.3f);
    fourthSphereTransform = Mat4.multiply(fourthSphereTransform, Mat4Transform.translate(-3, 0.5f, 0));
    TransformNode fourthSphereTransformNode = new TransformNode("scale(0.3f,1.5f,0.3f);translate(0,0.5,0)", fourthSphereTransform);
    ModelNode fourthSphereModelNode = new ModelNode("Sphere(4)", sphere);
    
    Mat4 fifthSphereTransform = Mat4Transform.scale(0.3f, 1.5f, 0.3f);
    fifthSphereTransform = Mat4.multiply(fifthSphereTransform, Mat4Transform.translate(3, 0.5f, 0));
    TransformNode fifthSphereTransformNode = new TransformNode("scale(0.4f,1.8f,0.4f);translate(0,0.5,0)", fifthSphereTransform);
    ModelNode fifthSphereModelNode = new ModelNode("Sphere(5)", sphere);
  

    // Eyes
    Mat4 sixthSphereTransform = Mat4Transform.scale(0.7f, 0.5f, 0.5f);
    sixthSphereTransform = Mat4.multiply(sixthSphereTransform, Mat4Transform.translate(0.5f, 0.5f, 1.3f));
    TransformNode sixthSphereTransformNode = new TransformNode("scale(0.2f,1.0f,0.2f);translate(0,0.5,0)", sixthSphereTransform);
    ModelNode sixthSphereModelNode = new ModelNode("Sphere(6)", sphere);
    
    Mat4 seventhSphereTransform = Mat4Transform.scale(0.7f, 0.5f, 0.5f);
    seventhSphereTransform = Mat4.multiply(seventhSphereTransform, Mat4Transform.translate(-0.5f, 0.5f, 1.3f));
    TransformNode seventhSphereTransformNode = new TransformNode("scale(0.15f,0.8f,0.15f);translate(0,0.5,0)", seventhSphereTransform);
    ModelNode seventhSphereModelNode = new ModelNode("Sphere(7)", sphere);

    // Right arms
    Mat4 eighthSphereTransform = Mat4Transform.scale(0.3f, 1.5f, 0.3f);
    eighthSphereTransform = Mat4.multiply(eighthSphereTransform, Mat4Transform.translate(-2f, 2f, 0f));
    TransformNode eighthSphereTransformNode = new TransformNode("scale(0.4f,0.4f,0.4f);translate(-1.5f,0.5,0)", eighthSphereTransform);
    ModelNode eighthSphereModelNode = new ModelNode("Sphere(8)", sphere);

    // Left arms
    Mat4 ninthSphereTransform = Mat4Transform.scale(0.3f, 1.5f, 0.3f);
    ninthSphereTransform = Mat4.multiply(ninthSphereTransform, Mat4Transform.translate(2f, 2f, 0f));
    TransformNode ninthSphereTransformNode = new TransformNode("scale(0.4f,0.4f,0.4f);translate(1.5f,0.5,0)", ninthSphereTransform);
    ModelNode ninthSphereModelNode = new ModelNode("Sphere(9)", sphere);

    twoBranchRoot.addChild(translateX);
      translateX.addChild(rotateAll);
        rotateAll.addChild(lowerBranch);
          lowerBranch.addChild(makeLowerBranch);
            makeLowerBranch.addChild(cube0Node);
              // Arm, Left
              rotateAll.addChild(eighthSphereTransformNode); // since arms need to be moved together
              eighthSphereTransformNode.addChild(eighthSphereModelNode);
              // Rotate
              rotateAll.addChild(rotateUpperArm2);
              rotateUpperArm2.addChild(eighthSphereTransformNode);
              // Arm, Right
              rotateAll.addChild(ninthSphereTransformNode);
              ninthSphereTransformNode.addChild(ninthSphereModelNode);
              // Rotate
              rotateAll.addChild(rotateUpperArm);
              rotateUpperArm.addChild(ninthSphereTransformNode);

          lowerBranch.addChild(translateToTop);
            translateToTop.addChild(rotateUpper);
              rotateUpper.addChild(upperBranch);
                upperBranch.addChild(makeUpperBranch);
                  makeUpperBranch.addChild(cube1Node);
        
          // Horn
          cube1Node.addChild(newSphereTransformNode);
          newSphereTransformNode.addChild(newSphereModelNode);
          // Top of Horn
          upperBranch.addChild(thirdSphereTransformNode);
          thirdSphereTransformNode.addChild(thirdSphereModelNode);
          // Ear, Right
          upperBranch.addChild(fourthSphereTransformNode);
          fourthSphereTransformNode.addChild(fourthSphereModelNode);
          // Ear, Left
          upperBranch.addChild(fifthSphereTransformNode);
          fifthSphereTransformNode.addChild(fifthSphereModelNode);
          // Eye, Left
          upperBranch.addChild(sixthSphereTransformNode);
          sixthSphereTransformNode.addChild(sixthSphereModelNode);
          // Eye, Right
          upperBranch.addChild(seventhSphereTransformNode);
          seventhSphereTransformNode.addChild(seventhSphereModelNode);

      twoBranchRoot.update(); // IMPORTANT must be done every time any part of the scene graph changes
      // twoBranchRoot.print(0, false);
      // System.exit(0);


    // The Other Alien
    mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
    shader = new Shader(gl, "vs_sphere_04.txt", "fs_sphere_04.txt");
    material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f), new Vec3(0.5f, 0.5f, 0.5f),
        32.0f);
    modelMatrix = Mat4.multiply(Mat4Transform.scale(4, 4, 4), Mat4Transform.translate(0, 0.5f, 0));
    sphere = new Model(gl, camera, light, shader, material, modelMatrix, mesh, textureId9, textureId10);

    twoBranchRoot2 = new NameNode("two-branch structure");
    translateX2 = new TransformNode("translate(" + xPosition2 + ",0,0)", Mat4Transform.translate(xPosition2, 0, 0));
    rotateAll2 = new TransformNode("rotateAroundZ(" + rotateAllAngle + ")", Mat4Transform.rotateAroundZ(rotateAllAngle));
    NameNode lowerBranch2 = new NameNode("lower branch");
    Mat4 m3 = Mat4Transform.scale(3, 3, 3);
    m3 = Mat4.multiply(m3, Mat4Transform.translate(0, 0.5f, 0));
    TransformNode makeLowerBranch2 = new TransformNode("scale(2.5,4,2.5); translate(0,0.5,0)", m3);
    ModelNode cube0Node2 = new ModelNode("Sphere(0)", sphere);
    TransformNode translateToTop2 = new TransformNode("translate(0,4,0)", Mat4Transform.translate(0, 4, 0));
    rotateUpper2 = new TransformNode("rotateAroundZ(" + rotateUpperAngle + ")",Mat4Transform.rotateAroundZ(rotateUpperAngle));
    rotateUpperArm_2 = new TransformNode("rotateAroundZ(" + rotateUpperAngleArm + ")",Mat4Transform.rotateAroundZ(rotateUpperAngleArm));
    rotateUpperArm2_2 = new TransformNode("rotateAroundZ(" + rotateUpperAngleArm2 + ")",Mat4Transform.rotateAroundZ(rotateUpperAngleArm2));
    NameNode upperBranch2 = new NameNode("upper branch");
    m3 = Mat4Transform.scale(2f, 2f, 2f);
    m3 = Mat4.multiply(m, Mat4Transform.translate(0, 0, 0));
    TransformNode makeUpperBranch2 = new TransformNode("scale(1.4f,3.1f,1.4f);translate(0,0.5,0)", m3);
    ModelNode cube1Node2 = new ModelNode("Sphere(1)", sphere);

    // Horn
    Mat4 newSphereTransform2 = Mat4Transform.scale(0.1f, 1.0f, 0.1f);
    newSphereTransform2 = Mat4.multiply(newSphereTransform2, Mat4Transform.translate(0, 0.5f, 0));
    TransformNode newSphereTransformNode2 = new TransformNode("scale(1.4f,3.9f,1.4f);translate(0,0.5,0)", newSphereTransform2);
    ModelNode newSphereModelNode2 = new ModelNode("Sphere(2)", sphere);

    // Top of horn
    Mat4 thirdSphereTransform2 = Mat4Transform.scale(0.3f, 0.3f, 0.3f);
    thirdSphereTransform2 = Mat4.multiply(thirdSphereTransform2, Mat4Transform.translate(0, 7.1f, 0));
    TransformNode thirdSphereTransformNode2 = new TransformNode("scale(0.25f,2.0f,0.25f);translate(0,0.5,0)", thirdSphereTransform2);
    ModelNode thirdSphereModelNode2 = new ModelNode("Sphere(3)", sphere);

    // ears
    Mat4 fourthSphereTransform2 = Mat4Transform.scale(0.3f, 1.5f, 0.3f);
    fourthSphereTransform2 = Mat4.multiply(fourthSphereTransform2, Mat4Transform.translate(-3, 0.5f, 0));
    TransformNode fourthSphereTransformNode2 = new TransformNode("scale(0.3f,1.5f,0.3f);translate(0,0.5,0)", fourthSphereTransform2);
    ModelNode fourthSphereModelNode2 = new ModelNode("Sphere(4)", sphere);
    
    Mat4 fifthSphereTransform2 = Mat4Transform.scale(0.3f, 1.5f, 0.3f);
    fifthSphereTransform2 = Mat4.multiply(fifthSphereTransform2, Mat4Transform.translate(3, 0.5f, 0));
    TransformNode fifthSphereTransformNode2 = new TransformNode("scale(0.4f,1.8f,0.4f);translate(0,0.5,0)", fifthSphereTransform2);
    ModelNode fifthSphereModelNode2 = new ModelNode("Sphere(5)", sphere);
  

    // eyes
    Mat4 sixthSphereTransform2 = Mat4Transform.scale(0.7f, 0.5f, 0.5f);
    sixthSphereTransform2 = Mat4.multiply(sixthSphereTransform2, Mat4Transform.translate(0.5f, 0.5f, 1.3f));
    TransformNode sixthSphereTransformNode2 = new TransformNode("scale(0.2f,1.0f,0.2f);translate(0,0.5,0)", sixthSphereTransform2);
    ModelNode sixthSphereModelNode2 = new ModelNode("Sphere(6)", sphere);
    
    Mat4 seventhSphereTransform2 = Mat4Transform.scale(0.7f, 0.5f, 0.5f);
    seventhSphereTransform2 = Mat4.multiply(seventhSphereTransform2, Mat4Transform.translate(-0.5f, 0.5f, 1.3f));
    TransformNode seventhSphereTransformNode2 = new TransformNode("scale(0.15f,0.8f,0.15f);translate(0,0.5,0)", seventhSphereTransform2);
    ModelNode seventhSphereModelNode2 = new ModelNode("Sphere(7)", sphere);

    // Right arms
    Mat4 eighthSphereTransform2 = Mat4Transform.scale(0.3f, 1.5f, 0.3f);
    eighthSphereTransform2 = Mat4.multiply(eighthSphereTransform2, Mat4Transform.translate(-2f, 2f, 0f));
    TransformNode eighthSphereTransformNode2 = new TransformNode("scale(0.4f,0.4f,0.4f);translate(-1.5f,0.5,0)", eighthSphereTransform2);
    ModelNode eighthSphereModelNode2 = new ModelNode("Sphere(8)", sphere);

    // Left arms
    Mat4 ninthSphereTransform2 = Mat4Transform.scale(0.3f, 1.5f, 0.3f);
    ninthSphereTransform2 = Mat4.multiply(ninthSphereTransform2, Mat4Transform.translate(2f, 2f, 0f));
    TransformNode ninthSphereTransformNode2 = new TransformNode("scale(0.4f,0.4f,0.4f);translate(1.5f,0.5,0)", ninthSphereTransform2);
    ModelNode ninthSphereModelNode2 = new ModelNode("Sphere(9)", sphere);

    twoBranchRoot2.addChild(translateX2);
      translateX2.addChild(rotateAll2);
        rotateAll2.addChild(lowerBranch2);
          lowerBranch2.addChild(makeLowerBranch2);
            makeLowerBranch2.addChild(cube0Node2);
              // Arm, Left
              rotateAll2.addChild(eighthSphereTransformNode2); // since arms need to be moved together
              eighthSphereTransformNode2.addChild(eighthSphereModelNode2);
              // Rotate
              rotateAll2.addChild(rotateUpperArm2_2);
              rotateUpperArm2_2.addChild(eighthSphereTransformNode2);
              // Arm, Right
              rotateAll2.addChild(ninthSphereTransformNode2);
              ninthSphereTransformNode2.addChild(ninthSphereModelNode2);
              // Rotate
              rotateAll2.addChild(rotateUpperArm_2);
              rotateUpperArm_2.addChild(ninthSphereTransformNode2);

     
        lowerBranch2.addChild(translateToTop2);
            translateToTop2.addChild(rotateUpper2);
              rotateUpper2.addChild(upperBranch2);
                upperBranch2.addChild(makeUpperBranch2);
                  makeUpperBranch2.addChild(cube1Node2);  
                  
          // Horn
          cube1Node2.addChild(newSphereTransformNode2);
          newSphereTransformNode2.addChild(newSphereModelNode2);
          // Top of Horn
          upperBranch2.addChild(thirdSphereTransformNode2);
          thirdSphereTransformNode2.addChild(thirdSphereModelNode2);
          // Ear, Right
          upperBranch2.addChild(fourthSphereTransformNode2);
          fourthSphereTransformNode2.addChild(fourthSphereModelNode2);
          // Ear, Left
          upperBranch2.addChild(fifthSphereTransformNode2);
          fifthSphereTransformNode2.addChild(fifthSphereModelNode2);
          // Eye, Left
          upperBranch2.addChild(sixthSphereTransformNode2);
          sixthSphereTransformNode2.addChild(sixthSphereModelNode2);
          // Eye, Right
          upperBranch2.addChild(seventhSphereTransformNode2);
          seventhSphereTransformNode2.addChild(seventhSphereModelNode2);    
    
    twoBranchRoot2.update();

  }
  private void render(GL3 gl) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    light.setPosition(getLightPosition()); // changing light position each frame
    light.render(gl);
    gen_light.setPosition(getLightPosition_gen());
    gen_light.render(gl);
    gen_light2.setPosition(getLightPosition_gen2());
    gen_light2.render(gl);
    floor.render(gl);
    pole.render(gl);
    updateBranches();
    updateBranches2();
    twoBranchRoot.draw(gl);
    twoBranchRoot2.draw(gl);
    wallBranchRoot.draw(gl);
  }
  // Animations
  // Rock
  public void updateBranches() {
      if (updateBranchesEnabled) {
      double elapsedTime = getSeconds() - startTime;
      rotateAllAngle = rotateAllAngleStart * (float) Math.sin(elapsedTime);
      rotateAll.setTransform(Mat4Transform.rotateAroundZ(rotateAllAngle));
      rotateAll2.setTransform(Mat4Transform.rotateAroundZ(rotateAllAngle));
      twoBranchRoot.update(); // IMPORTANT the scene graph has changed
      twoBranchRoot2.update();
    }
  }
  public void toggleUpdateBranches() {
    updateBranchesEnabled = !updateBranchesEnabled;
  }

  // Roll
   public void updateBranches2() {
      if (updateBranchesEnabled2) {
      double elapsedTime = getSeconds() - startTime;
      rotateUpperAngle = rotateUpperAngleStart * (float) Math.sin(elapsedTime * 0.5f);
      rotateUpper.setTransform(Mat4Transform.rotateAroundZ(rotateUpperAngle));
      rotateUpper2.setTransform(Mat4Transform.rotateAroundZ(rotateUpperAngle));
      twoBranchRoot.update(); // IMPORTANT the scene graph has changed
      twoBranchRoot2.update();
    }
  }
  public void toggleUpdateBranches2() {
    updateBranchesEnabled2 = !updateBranchesEnabled2;
  }


  // The light's postion is continually being changed, so needs to be calculated
  // for each frame.
  private Vec3 getLightPosition() {
    if (isLightOn) {
    double elapsedTime = getSeconds() - startTime;
    float x = -5 + -3.0f * (float) (Math.sin(Math.toRadians(elapsedTime * 50)));
    float y = 13f;
    float z = 5.0f * (float) (Math.cos(Math.toRadians(elapsedTime * 50)));
    return new Vec3(x, y, z);
    }
    else {
      return new Vec3(5000f, -5000f, 5000f);
    }
  }
  public void toggleLightOn(){
    isLightOn = !isLightOn;
  }
  // General Lights position
  private Vec3 getLightPosition_gen() {
    float x = 10f;
    float y = 15f;
    float z = -10f;
    return new Vec3(x, y, z);
  }
  private Vec3 getLightPosition_gen2() {
    float x = -10f;
    float y = 15f;
    float z = -10f;
    return new Vec3(x, y, z);
  }
  // ***************************************************
  /*
   * TIME
   */

  private double startTime;

  private double getSeconds() {
    return System.currentTimeMillis() / 1000.0;
  }

  // ***************************************************
  /*
   * An array of random numbers
   */

  private int NUM_RANDOMS = 1000;
  private float[] randoms;

  private void createRandomNumbers() {
    randoms = new float[NUM_RANDOMS];
    for (int i = 0; i < NUM_RANDOMS; ++i) {
      randoms[i] = (float) Math.random();
    }
  }

}