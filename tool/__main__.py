if __name__ == '__main__':
    try:
        from cli import main
        main()
    except KeyboardInterrupt:
        exit(1)
