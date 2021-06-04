#!/bin/bash
set -o errexit
set -o pipefail

DIST=trusty

cd "$(dirname "$0")"
basedir=$PWD
mkdir -p target/lib-tmp
cd target/lib-tmp

version=$(sed -e '/<zimbra.version>/!d' -e 's|^[^>]*>||' -e 's|<.*$||' "$basedir/pom.xml")
test -n "$version"

case "$version" in
    8.7.*)
        repo=87
        false
        ;;
    8.8.*)
        repo=${version//.}
        package=zimbra-common-core-jar
        ;;
    *)
        false
        ;;
esac
test -n "$package"

baseurl=https://repo.zimbra.com/apt/$repo

echo -n > package.list
declare -A item
curl -s "$baseurl/dists/$DIST/zimbra/binary-amd64/Packages" | while read key value; do
    key=${key,,}
    if [[ -n "$key" ]]; then
        item[${key%:}]=$value
        continue
    fi
    if [[ "${item[package]}" == "$package" ]]; then
        path=$(dirname "${item[filename]}")
        file=$(basename "${item[filename]}")
        echo "${item[version]} $file $path ${item[sha256]}" >> package.list
    fi
    item=()
done

rm -f package.sha256
sort -V package.list | tail -n 1 | while read version file path sha256; do
    echo "$sha256 *$file" > package.sha256
    xargs -n 1 curl -L -o "$file.part" <<< "$baseurl/$path/$file"
    mv "$file.part" "$file"
    sha256sum -c package.sha256 >/dev/null
done

if [[ -s package.sha256 ]]; then
    deb=$(cut -d '*' -f 2 package.sha256)
else
    case $DIST in
        trusty)
            v=14
            ;;
        xenial)
            v=16
            ;;
        bionic)
            v=18
            ;;
        *)
            false
            ;;
    esac
    make -C "$basedir/src/test/vagrant/zcs" VERSION=${version}
    deb=$(ls -1 $basedir/src/test/vagrant/zcs/zcs-${version}_GA_*.UBUNTU${v}_64.*/packages/${package}_${version}.*.u${v}_amd64.deb | head -n 1)
fi
ar x "$deb"

if [[ -d opt ]]; then
    rm -r opt
fi
tar tfa data.tar.xz | grep /opt/zimbra/lib/jars/zimbra | xargs tar xfa data.tar.xz

chmod +w opt/zimbra/lib/jars/*.jar
rm -f $basedir/lib/*.jar
mv opt/zimbra/lib/jars/*.jar "$basedir/lib"
