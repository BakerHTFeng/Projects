# -*- mode: python -*-

block_cipher = None


a = Analysis(['VissimSimulator.py'],
             pathex=['D:\\Programming\\Git\\Projects\\VissimSimulator'],
             binaries=[],
             datas=[],
             hiddenimports=[],
             hookspath=[],
             runtime_hooks=[],
             excludes=[],
             win_no_prefer_redirects=False,
             win_private_assemblies=False,
             cipher=block_cipher)
pyz = PYZ(a.pure, a.zipped_data,
             cipher=block_cipher)
exe = EXE(pyz,
          a.scripts,
          a.binaries,
          a.zipfiles,
          a.datas,
          name='VissimSimulator',
          debug=False,
          strip=False,
          upx=True,
          console=False , icon='icon\\VissimSimulator.ico')
