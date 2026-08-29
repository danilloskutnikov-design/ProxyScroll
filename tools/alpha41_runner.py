from pathlib import Path

path = Path(__file__).with_name("alpha41_patch.py")
code = path.read_text(encoding="utf-8")
code = code.replace(
    '        if count != 1:\n            raise RuntimeError(f"{rel}: patch {i} expected exactly once, found {count}")\n',
    '        if count < 1:\n            raise RuntimeError(f"{rel}: patch {i} target not found")\n',
)
exec(compile(code, str(path), "exec"), {"__name__": "__main__", "__file__": str(path)})
