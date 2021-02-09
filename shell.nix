let
  sources = import ./nix/sources.nix;
  pkgs = import sources.nixpkgs {};
in
pkgs.mkShell {
  buildInputs = [
    pkgs.openjfx11
    pkgs.openjdk11
    pkgs.clojure
  ];
}
