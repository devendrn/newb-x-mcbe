if __name__ == '__main__':
    try:
        import importlib.metadata
        from packaging.version import parse as parse_ver
        with open("requirements.txt", 'r') as file:
            for ln in file:
                ln = ln.strip().replace(" ", "").split("==")
                ver = importlib.metadata.version(ln[0])
                if parse_ver(ver) != parse_ver(ln[1]):
                    print(f'Warning! Using incompatible version {ver} of "{ln[0]}" instead of {ln[1]}')
        from cli import main
        main()
    except importlib.metadata.PackageNotFoundError as e:
        print(f'Module "{e.name}" not found!')
        print('\nDid you run `python -m pip install -r requirements.txt`?\n')
        exit(1)
    except KeyboardInterrupt:
        exit(1)
    except Exception as e:
        print(e)
        exit(1)
