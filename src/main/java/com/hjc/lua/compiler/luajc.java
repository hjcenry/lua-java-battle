package com.hjc.lua.compiler;
/*******************************************************************************
 * Copyright (c) 2009-2012 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.lang3.StringUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.luaj.vm2.luajc.LuaJC;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Compiler for lua files to compile lua sources or lua binaries into java classes.
 */
public class luajc {
    private static final String version = Lua._VERSION + " Copyright (C) 2012 luaj.org";

    private static final String usage =
            "usage: java -cp luaj-jse.jar,bcel-5.2.jar luajc [options] fileordir [, fileordir ...]\n" +
                    "Available options are:\n" +
                    "  -        process stdin\n" +
                    "  -s src	source directory\n" +
                    "  -i	    ignore files\n" +
                    "  -d dir	destination directory\n" +
                    "  -p pkg	package prefix to apply to all classes\n" +
                    "  -m		generate main(String[]) function for JSE\n" +
                    "  -r		recursively compile all\n" +
                    "  -l		load classes to verify generated bytecode\n" +
                    "  -c enc  	use the supplied encoding 'enc' for input files\n" +
                    "  -v   	verbose\n";

    private static void usageExit() {
        System.err.println(usage);
        System.exit(-1);
    }

    private String destDir = ".";
    private boolean genMain = false;
    private boolean recurse = false;
    private boolean verbose = false;
    private boolean loadClasses = false;
    private String encoding = null;
    private String pkgPrefix = null;
    private String srcDir = ".";
    private final List<InputFile> files = new ArrayList<>();
    private final List<String> ignoreFileNames = new ArrayList<>();
    private final Globals globals;
    private final Logger logger;

    public static void compile(List<KeyValue<String, String>> keyValues, Logger logger) {
        new luajc(keyValues, logger);
    }

    private luajc(List<KeyValue<String, String>> keyValues, Logger logger) {
        this.logger = logger;

        // process args
        List<String> seeds = new ArrayList<>();

        // get stateful args
        if (!CollectionUtils.isEmpty(keyValues)) {
            for (KeyValue<String, String> keyValue : keyValues) {
                String key = keyValue.getKey();
                String value = keyValue.getValue();
                if (!key.startsWith("-")) {
                    seeds.add(key);
                } else {
                    switch (key.charAt(1)) {
                        case 'i':
                            Collections.addAll(ignoreFileNames, value.split(";"));
                            break;
                        case 's':
                            srcDir = value;
                            break;
                        case 'd':
                            destDir = value;
                            break;
                        case 'l':
                            loadClasses = true;
                            break;
                        case 'p':
                            pkgPrefix = value;
                            break;
                        case 'm':
                            genMain = true;
                            break;
                        case 'r':
                            recurse = true;
                            break;
                        case 'c':
                            encoding = value;
                            break;
                        case 'v':
                            verbose = true;
                            break;
                        default:
                            usageExit();
                            break;
                    }
                }
            }
        }

        // echo version
        if (verbose) {
            this.logger.info(version);
            this.logger.info("lua目录\t: " + srcDir);
            this.logger.info("class目录\t: " + destDir);
            this.logger.info("编译lua文件:\t " + seeds);
            this.logger.info("递归:\t" + recurse);
        }

        // need at least one seed
        if (seeds.size() <= 0) {
            this.logger.error(usage);
            System.exit(-1);
        }

        // collect up files to process
        for (String seed : seeds) {
            collectFiles(srcDir + "/" + seed);
        }

        // check for at least one file
        if (files.size() <= 0) {
            this.logger.error("no files found in " + seeds);
            System.exit(-1);
        }

        // process input files
        globals = JsePlatform.standardGlobals();
        for (InputFile file : files) {
            processFile(file);
        }
    }

    private void collectFiles(String path) {
        File f = new File(path);
        if (f.isDirectory() && recurse) {
            scanDir(f, pkgPrefix);
        } else if (f.isFile()) {
            File dir = f.getAbsoluteFile().getParentFile();
            if (dir != null) {
                scanFile(f, pkgPrefix);
            }
        }
    }

    private void scanDir(File dir, String javapackage) {
        File[] f = dir.listFiles();
        if (f == null) {
            return;
        }

        String dirPath = dir.getAbsolutePath();
        for (String ignoreFileName : ignoreFileNames) {
            if (StringUtils.isEmpty(ignoreFileName)) {
                continue;
            }
            if (ignoreFileName.endsWith(".lua")) {
                continue;
            }
            String ignoreDirFile = new File(srcDir + ignoreFileName).getAbsolutePath();
            if (ignoreDirFile.equals(dirPath)) {
                // 忽略目录
                if (verbose) {
                    this.logger.warn("忽略目录：" + dirPath);
                }
                return;
            }
        }

        for (File file : f) {
            scanFile(file, javapackage);
        }
    }

    private void scanFile(File f, String javaPackage) {
        if (f.exists()) {
            if (f.isDirectory() && recurse) {
                scanDir(f, (javaPackage != null ? javaPackage + "." + f.getName() : f.getName()));
            } else if (f.isFile() && f.getName().endsWith(".lua")) {
                for (String ignoreFileName : ignoreFileNames) {
                    if (StringUtils.isEmpty(ignoreFileName)) {
                        continue;
                    }
                    if (ignoreFileName.equals(f.getName())) {
                        // 忽略文件
                        if (verbose) {
                            this.logger.warn("忽略文件：" + ignoreFileName);
                        }
                        return;
                    }
                }
                files.add(new InputFile(f, javaPackage));
            }
        }
    }

    private static final class LocalClassLoader extends ClassLoader {
        private final Hashtable<?, ?> t;

        private LocalClassLoader(Hashtable<?, ?> t) {
            this.t = t;
        }

        @Override
        public Class<?> findClass(String classname) throws ClassNotFoundException {
            byte[] bytes = (byte[]) t.get(classname);
            if (bytes != null) {
                return defineClass(classname, bytes, 0, bytes.length);
            }
            return super.findClass(classname);
        }
    }

    class InputFile {
        public String luaChunkName;
        public String srcFileName;
        public File inFile;
        public File outDir;
        public String javaPackage;

        public InputFile(File f, String javaPackage) {
            this.inFile = f;
            String subDir = javaPackage != null ? javaPackage.replace('.', '/') : null;
            String outDirPath = subDir != null ? destDir + "/" + subDir : destDir;
            this.javaPackage = javaPackage;
            this.srcFileName = (subDir != null ? subDir + "/" : "") + inFile.getName();
            this.luaChunkName = (subDir != null ? subDir + "/" : "") + inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
            this.inFile = f;
            this.outDir = new File(outDirPath);
        }
    }

    private void processFile(InputFile inf) {
        if (!inf.outDir.mkdirs()) {
            return;
        }
        try {
            if (verbose) {
                this.logger.info("目标文件=" + inf.luaChunkName + " lua源文件=" + inf.srcFileName);
            }

            // create the chunk
            FileInputStream fis = new FileInputStream(inf.inFile);

            String luaChunkName = inf.luaChunkName;
            if (luaChunkName.contains("/")) {
                luaChunkName = luaChunkName.substring(luaChunkName.lastIndexOf("/") + 1);
            }
            String srcFileName = inf.srcFileName;

            final Hashtable<?, ?> t = encoding != null ?
                    LuaJC.instance.compileAll(new InputStreamReader(fis, encoding), luaChunkName, srcFileName, globals, genMain) :
                    LuaJC.instance.compileAll(fis, luaChunkName, srcFileName, globals, genMain);
            fis.close();

            // write out the chunk
            for (Enumeration<?> e = t.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                byte[] bytes = (byte[]) t.get(key);
                if (key.indexOf('/') >= 0) {
                    String d = (destDir != null ? destDir + "/" : "") + key.substring(0, key.lastIndexOf('/'));
                    new File(d).mkdirs();
                }

                String dirPath = inf.outDir.getAbsolutePath();
                String destPath = dirPath + "/" + key + ".class";

                if (verbose) {
                    this.logger.info("  " + destPath + " (" + bytes.length + " bytes)");
                }
                FileOutputStream fos = new FileOutputStream(destPath);
                fos.write(bytes);
                fos.close();
            }

            // try to load the files
            if (loadClasses) {
                ClassLoader loader = new LocalClassLoader(t);
                for (Enumeration<?> e = t.keys(); e.hasMoreElements(); ) {
                    String classname = (String) e.nextElement();
                    try {
                        Class<?> c = loader.loadClass(classname);
                        Object o = c.newInstance();
                        if (verbose) {
                            this.logger.info("    load类文件[" + classname + "] - " + o);
                        }
                    } catch (Exception ex) {
                        this.logger.error("    load类文件[" + classname + "]失败: " + ex);
                    }
                }
            }

        } catch (Exception e) {
            this.logger.error("    load类文件[" + inf.srcFileName + "]失败: " + e);
            e.printStackTrace(System.err);
            System.err.flush();
        }
    }
}
