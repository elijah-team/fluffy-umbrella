package tripleo.elijah.comp.specs;

import java.io.File;
import java.io.InputStream;

public record ElijahSpec(String f, File file, InputStream s, boolean do_out) {
}
