package com.example.football.util;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;

public interface XmlParse {

    <T> T fromFile(String filePath, Class<T> tClass) throws JAXBException, FileNotFoundException;

}
