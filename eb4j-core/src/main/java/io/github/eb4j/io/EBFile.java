package io.github.eb4j.io;

import java.io.File;

import org.apache.commons.lang.ArrayUtils;

import io.github.eb4j.EBException;

/**
 * ファイルおよびディレクトリの抽象表現クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class EBFile {

    /** ファイル情報 */
    private FileInfo _info = null;
    /** オリジナルファイル名 */
    private String _name = null;

    /**
     * コンストラクタ。
     *
     * @param dir ディレクトリ
     * @param name ファイル名
     * @param defaultFormat デフォルトのフォーマット形式
     * @exception EBException ファイルが存在しない場合
     * @see EBFormat#FORMAT_PLAIN
     * @see EBFormat#FORMAT_EBZIP
     * @see EBFormat#FORMAT_EPWING
     * @see EBFormat#FORMAT_EPWING6
     */
    public EBFile(final File dir, final String name,
                  final EBFormat defaultFormat) throws EBException {
        super();
        _info = new FileInfo();

        String ebzName = name + ".ebz";
        String orgName = name + ".org";
        String[] list = dir.list();
        if (!ArrayUtils.isEmpty(list)) {
            int len = list.length;
            for (int i = 0; i < len; i++) {
                File f = new File(dir, list[i]);
                if (f.isDirectory()) {
                    continue;
                }
                if (list[i].equalsIgnoreCase(name)) {
                    _info.setFile(f);
                    _info.setFormat(defaultFormat);
                    _name = list[i];
                    break;
                } else if (list[i].equalsIgnoreCase(orgName)) {
                    _info.setFile(f);
                    _info.setFormat(EBFormat.FORMAT_PLAIN);
                    _name = list[i].substring(0, list[i].length() - 4);
                    break;
                } else if (list[i].equalsIgnoreCase(ebzName)) {
                    _info.setFile(f);
                    _info.setFormat(EBFormat.FORMAT_EBZIP);
                    _name = list[i].substring(0, list[i].length() - 4);
                    break;
                }
            }
        }
        if (_info.getFile() == null) {
            throw new EBException(EBException.FILE_NOT_FOUND, dir.getPath(), name);
        }
        if (!_info.getFile().canRead()) {
            throw new EBException(EBException.CANT_READ_FILE, _info.getPath());
        }
        BookInputStream bis = getInputStream();
        try {
            bis.initFileInfo();
        } finally {
            bis.close();
        }
    }


    /**
     * 指定されたディレクトリ内から指定されたディレクトリを
     * 大文字/小文字の区別なく検索します。
     *
     * @param path ディレクトリパス
     * @param name ディレクトリ名
     * @return File Directory object.
     * @exception EBException ファイルが存在しない場合
     */
    public static File searchDirectory(final String path, final String name) throws EBException {
        return searchDirectory(new File(path), name);
    }

    /**
     * 指定されたディレクトリ内から指定されたディレクトリを
     * 大文字/小文字の区別なく検索します。
     *
     * @param dir ディレクトリ
     * @param name ディレクトリ名
     * @return File directory object.
     * @exception EBException ファイルが存在しない場合
     */
    public static File searchDirectory(final File dir, final String name) throws EBException {
        String[] list = dir.list();
        File d = null;
        if (!ArrayUtils.isEmpty(list)) {
            int len = list.length;
            for (int i=0; i<len; i++) {
                File f = new File(dir, list[i]);
                if (!f.isDirectory()) {
                    continue;
                }
                if (list[i].equalsIgnoreCase(name)) {
                    d = f;
                    break;
                }
            }
        }
        if (d == null) {
            throw new EBException(EBException.DIR_NOT_FOUND, dir.getPath(), name);
        }
        if (!d.canRead()) {
            throw new EBException(EBException.CANT_READ_DIR, dir.getPath());
        }
        return d;
    }

    /**
     * このオブジェクトのファイルを返します。
     *
     * @return ファイル
     */
    public File getFile() {
        return _info.getFile();
    }

    /**
     * このファイルのパス名を返します。
     *
     * @return ファイルのパス名
     */
    public String getPath() {
        return _info.getPath();
    }

    /**
     * このファイルのオリジナル名を返します。
     *
     * @return オリジナルファイル名
     */
    public String getName() {
        return _name;
    }

    /**
     * このファイルのフォーマット形式を返します。
     *
     * @return フォーマット形式
     * @see EBFormat#FORMAT_PLAIN
     * @see EBFormat#FORMAT_EBZIP
     * @see EBFormat#FORMAT_EPWING
     * @see EBFormat#FORMAT_EPWING6
     */
    public EBFormat getFormat() {
        return _info.getFormat();
    }

    /**
     * S-EBXAの圧縮情報を設定します。
     *
     * @param index 圧縮本文データインデックス開始位置
     * @param base 圧縮本文データ開始位置
     * @param start 本文開始位置
     * @param end 本文終了位置
     */
    public void setSEBXAInfo(final long index, final long base, final long start, final long end) {
        _info.setSebxaIndexPosition(index);
        _info.setSebxaBasePosition(base);
        _info.setSebxaStartPosition(start);
        _info.setSebxaEndPosition(end);
        _info.setFileSize(end);
        _info.setFormat(EBFormat.FORMAT_SEBXA);
    }

    /**
     * このファイルのデータ読み込みストリームを返します。
     *
     * @return データ読み込みストリーム (ディレクトリの場合はnull)
     * @exception EBException 入出力エラーが発生した場合
     */
    public BookInputStream getInputStream() throws EBException {
        BookInputStream bis = null;
        switch (_info.getFormat()) {
            case FORMAT_EBZIP:
                bis = new EBZipInputStream(_info);
                break;
            case FORMAT_EPWING:
            case FORMAT_EPWING6:
                bis = new EPWINGInputStream(_info);
                break;
            case FORMAT_SEBXA:
                bis = new SEBXAInputStream(_info);
                break;
            case FORMAT_PLAIN:
            default:
                bis = new PlainInputStream(_info);
                break;
        }
        return bis;
    }
}

// end of EBFile.java
